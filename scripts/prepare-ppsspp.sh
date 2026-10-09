#!/usr/bin/env bash
# =============================================================================
# VERTIX PSP Emulator — Preparar el núcleo PPSSPP localmente
# -----------------------------------------------------------------------------
# Este script automatiza la integración local del core de PPSSPP dentro de
# libvertix_core.so. Replica en tu máquina lo que hace el workflow
# `.github/workflows/integrate-ppsspp-core.yml` en CI.
#
# Uso:
#   scripts/prepare-ppsspp.sh                    # ref por defecto (master)
#   scripts/prepare-ppsspp.sh v1.18.1            # tag específico
#   scripts/prepare-ppsspp.sh master arm64-v8a   # ref + ABI específica
#
# Requisitos:
#   - Android NDK r27+ configurado en $ANDROID_NDK_HOME
#   - CMake 3.22+ en $PATH
#   - git, bash 4+
# =============================================================================

set -euo pipefail

# --- Colores para log ------------------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log()  { echo -e "${BLUE}[VERTIX]${NC} $*"; }
ok()   { echo -e "${GREEN}[OK]${NC} $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $*"; }
err()  { echo -e "${RED}[ERR]${NC} $*" >&2; }

# --- Argumentos ------------------------------------------------------------
PPSSPP_REF="${1:-master}"
TARGET_ABI="${2:-arm64-v8a}"

# --- Variables derivadas ---------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
THIRD_PARTY_DIR="$PROJECT_ROOT/third_party"
PPSSPP_DIR="$THIRD_PARTY_DIR/ppsspp"
PREBUILT_DIR="$PROJECT_ROOT/app/src/main/cpp/prebuilt/$TARGET_ABI"

PPSSPP_REPO="https://github.com/hrydgard/ppsspp.git"

# --- Validaciones previas --------------------------------------------------
log "Verificando entorno..."

if [ -z "${ANDROID_NDK_HOME:-}" ]; then
  # Intenta localizar el NDK en ubicaciones comunes.
  for candidate in \
    "$HOME/Android/Sdk/ndk"/* \
    "$HOME/Library/Android/sdk/ndk"/* \
    "/usr/local/lib/android/sdk/ndk"/*; do
    if [ -d "$candidate" ]; then
      export ANDROID_NDK_HOME="$candidate"
      break
    fi
  done
fi

if [ -z "${ANDROID_NDK_HOME:-}" ]; then
  err "ANDROID_NDK_HOME no está definido y no se pudo autodetectar."
  err "Instala el NDK desde Android Studio → SDK Manager → SDK Tools → NDK."
  exit 1
fi
ok "NDK: $ANDROID_NDK_HOME"

if ! command -v cmake &>/dev/null; then
  err "CMake no está en PATH. Instálalo (apt install cmake / brew install cmake)."
  exit 1
fi
ok "CMake: $(cmake --version | head -1)"

# --- 1. Clonar PPSSPP si no existe ----------------------------------------
log "Preparando PPSSPP (ref=$PPSSPP_REF, ABI=$TARGET_ABI)..."

if [ -d "$PPSSPP_DIR/.git" ]; then
  warn "El directorio $PPSSPP_DIR ya existe como repo git."
  read -rp "¿Actualizar al ref $PPSSPP_REF? (y/N) " yn
  if [[ "$yn" =~ ^[Yy]$ ]]; then
    cd "$PPSSPP_DIR"
    git fetch --depth=1 origin "$PPSSPP_REF"
    git checkout "$PPSSPP_REF"
  else
    log "Manteniendo estado actual del repo."
  fi
else
  log "Clonando PPSSPP ($PPSSPP_REF) en $PPSSPP_DIR..."
  mkdir -p "$THIRD_PARTY_DIR"
  git clone --depth=1 --branch="$PPSSPP_REF" "$PPSSPP_REPO" "$PPSSPP_DIR"
fi

# --- 2. Inicializar submódulos críticos -----------------------------------
log "Inicializando submódulos críticos de PPSSPP..."
cd "$PPSSPP_DIR"
SUBMODULES=(
  "ffmpeg"
  "ext/glslang"
  "ext/snappy"
  "ext/zip"
  "ext/zstd"
  "ext/armips"
  "ext/discord-rpc"
  "ext/miniupnp"
  "ext/xxHash"
)
for sub in "${SUBMODULES[@]}"; do
  if [ -f ".gitmodules" ] && grep -q "path = $sub" .gitmodules 2>/dev/null; then
    log "  → $sub"
    git submodule update --init --depth=1 "$sub" || warn "No se pudo inicializar $sub"
  fi
done
ok "Submódulos listos"

# --- 3. Aplicar parches VERTIX si existen ---------------------------------
PATCH_DIR="$PROJECT_ROOT/scripts/patches/ppsspp"
if [ -d "$PATCH_DIR" ]; then
  log "Aplicando parches VERTIX desde $PATCH_DIR..."
  for patch in "$PATCH_DIR"/*.patch; do
    [ -f "$patch" ] || continue
    log "  → $(basename "$patch")"
    git apply --whitespace=fix "$patch" || warn "No se pudo aplicar $(basename "$patch") — ¿ya aplicado?"
  done
  ok "Parches aplicados"
else
  log "No hay parches que aplicar ($PATCH_DIR no existe)."
fi

# --- 4. Compilar libPPSSPPCore.a ------------------------------------------
BUILD_DIR="$PPSSPP_DIR/build-$TARGET_ABI"
mkdir -p "$BUILD_DIR"
cd "$BUILD_DIR"

log "Configurando CMake para $TARGET_ABI (Release)..."
cmake .. \
  -DCMAKE_TOOLCHAIN_FILE="$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake" \
  -DANDROID_ABI="$TARGET_ABI" \
  -DANDROID_PLATFORM=android-24 \
  -DANDROID_STL=c++_static \
  -DCMAKE_BUILD_TYPE=Release \
  -DHEADLESS=OFF \
  -DUNITTEST=OFF \
  -DUSE_DISCORD=OFF \
  -DUSE_SYSTEM_MINIUPNPC=OFF \
  -DUSE_SYSTEM_ZSTD=OFF \
  -DUSE_WAYLAND_WSI=OFF \
  -DANDROID=ON

log "Compilando libPPSSPPCore.a (esto puede tardar 20-40 min)..."
cmake --build . --target PPSSPPCore -j"$(nproc 2>/dev/null || echo 4)"

# --- 5. Copiar la static library al prebuilt dir --------------------------
log "Copiando libPPSSPPCore.a al directorio prebuilt..."
mkdir -p "$PREBUILT_DIR"
STATIC_LIB=$(find . -name "libPPSSPPCore.a" -print -quit)
if [ -z "$STATIC_LIB" ]; then
  err "No se encontró libPPSSPPCore.a tras la compilación."
  err "Verifica el log de CMake arriba."
  exit 1
fi
cp "$STATIC_LIB" "$PREBUILT_DIR/"
ls -lh "$PREBUILT_DIR/libPPSSPPCore.a"
ok "libPPSSPPCore.a instalada en $PREBUILT_DIR"

# --- 6. Verificación final ------------------------------------------------
log "Resumen de la integración:"
echo "  PPSSPP ref:        $PPSSPP_REF"
echo "  PPSSPP commit:     $(cd "$PPSSPP_DIR" && git rev-parse --short HEAD)"
echo "  ABI:               $TARGET_ABI"
echo "  Static lib:        $PREBUILT_DIR/libPPSSPPCore.a"
echo "  Tamaño:            $(du -h "$PREBUILT_DIR/libPPSSPPCore.a" | cut -f1)"
echo ""
ok "Listo. Ahora ejecuta: ./gradlew :app:assembleDebug"
warn "Recuerda: libvertix_core.so se enlazará contra libPPSSPPCore.a solo si"
warn "pasas -DVERTIX_LINK_PPSSPP=ON a CMake (ver CMakeLists.txt)."

# --- 7. Sugerencia para commit --------------------------------------------
cat <<EOF

Para registrar el puntero del submodule en el repo VERTIX:

  cd $PROJECT_ROOT
  git add third_party/ppsspp app/src/main/cpp/prebuilt
  git commit -m "build(ppsspp): integra core PPSSPP @ $(cd "$PPSSPP_DIR" && git rev-parse --short HEAD) ($TARGET_ABI)"

EOF
