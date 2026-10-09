# Configuración de Secretos en GitHub Actions

Este documento describe cómo configurar los secretos necesarios para que los workflows de CI de VERTIX funcionen correctamente.

## ⚠️ Regla de oro

**NUNCA pongas un token directamente en un archivo del repo (yaml, sh, txt, comentarios).**
Los tokens van SIEMPRE como secrets cifrados en GitHub Settings → Secrets and variables → Actions.

GitHub escanea automáticamente todos los commits en busca de tokens filtrados y los **revoca al instante**, incluso si están en una rama privada o en un comentario.

---

## Secretos requeridos

### `PAT_TOKEN` (Personal Access Token)

Token de acceso personal de GitHub con los siguientes permisos mínimos:

| Scope | Permisos | Uso |
|---|---|---|
| `repo` | Full control | Push de artefactos, gestión de releases |
| `workflow` | Update | Modificación de workflows desde CI (si aplica) |

#### Generación

1. Ve a https://github.com/settings/tokens?type=beta (Fine-grained) o
   https://github.com/settings/tokens/new (Classic).
2. Genera un token con los scopes de la tabla de arriba.
3. **Cópialo inmediatamente** — no volverás a verlo.
4. NO lo pegues en ningún archivo. Pásalo directo al formulario de secrets.

#### Registro como secret

1. En el repo VERTIX: **Settings → Secrets and variables → Actions → New repository secret**.
2. Name: `PAT_TOKEN`
3. Secret: pega el token.
4. Add secret.

#### Revocación de emergencia

Si en algún momento sospechas que el token se ha filtrado:

1. Ve a https://github.com/settings/tokens.
2. Localiza el token y pulsa **Delete / Revoke**.
3. Genera uno nuevo y actualiza el secret `PAT_TOKEN`.

> **Advertencia**: el token de un solo uso que el usuario pegó en el chat YA está
> comprometido y fue revocado automáticamente por GitHub Secret Scanning.
> NO intentes reutilizarlo — genera uno nuevo desde cero.

---

### `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD` (release builds)

Solo necesarios si quieres firmar releases desde CI. El `app/build.gradle.kts`
ya los lee de variables de entorno:

```yaml
env:
  KEYSTORE_PATH: ${{ secrets.KEYSTORE_PATH }}      # ruta en el runner al .jks
  STORE_PASSWORD: ${{ secrets.STORE_PASSWORD }}
  KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
```

Para subir el keystore como secret (base64):

```bash
base64 -w 0 my-upload-key.jks > keystore.b64
# Pega el contenido de keystore.b64 en un secret llamado KEYSTORE_BASE64.
```

En el workflow:

```yaml
- name: Decodificar keystore
  run: echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > $HOME/my-upload-key.jks
- name: Compilar release firmado
  env:
    KEYSTORE_PATH: $HOME/my-upload-key.jks
    STORE_PASSWORD: ${{ secrets.STORE_PASSWORD }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
  run: ./gradlew :app:assembleRelease
```

---

## Variables de entorno (no secretas)

Estas se configuran en **Settings → Secrets and variables → Actions → Variables** (no requieren cifrado):

| Variable | Valor | Uso |
|---|---|---|
| `NDK_VERSION` | `27.0.12077973` | Versión del NDK en CI |
| `CMAKE_VERSION` | `3.22.1` | Versión de CMake en CI |

---

## Protección de rama `main`

Para que el workflow `build-apk.yml` actúe realmente como gate antes del merge:

1. **Settings → Branches → Add branch protection rule**.
2. Branch name pattern: `main`.
3. Marca:
   - ✅ Require status checks to pass before merging.
   - ✅ Require branches to be up to date before merging.
   - Status checks requeridos:
     - `Build APK (debug)` (job `build`)
   - ✅ Require conversation resolution before merging.
   - ✅ Do not allow bypassing the above settings.
4. Save changes.

A partir de ahora, ningún PR puede mergearse a `main` sin que el build pase.

---

## Workflow de revocación del token de un solo uso

Si has pegado un token en el chat o en un archivo por error:

1. **Inmediato**: revoca el token en https://github.com/settings/tokens.
2. **Histórico**: GitHub Secret Scanning ya lo habrá detectado, pero si está en un commit:
   ```bash
   # NO uses git filter-branch (es lento y deja referencias colgantes).
   # Usa git-filter-repo:
   pip install git-filter-repo
   git filter-repo --invert-paths --path-regex 'path/al/archivo/comprometido'
   git push --force-with-lease
   ```
3. **Comunicación**: si el repo es público, avisa a los colaboradores que el
   historial fue reescrito y deben hacer `git pull --rebase`.
4. **Rotación**: genera un nuevo token y regenera el secret `PAT_TOKEN`.
