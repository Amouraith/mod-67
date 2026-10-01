# Mod 67 (Fabric 1.21.1)

Contenido (pestaña creativa "Mod 67"):
- Espada del 67: click derecho = MODO 67 (efectos 6,7 s, "67" amarillo gigante, 67 s de recarga).
- Armadura 67: dura 67 segundos puesta y se desintegra.
- Proyector: click derecho = MODO PROYECTOR. Llorás 1 s y luego, agachado (shift), rayos rojos por los ojos de 100 de daño.
- Rayos de "67" amarillos: con Modo 67 activo o armadura puesta, agachate (shift).
- Fragmento del 67 y Bloque 67 (salto épico con click derecho).

## Compilar SIN instalar nada (GitHub Actions)
1. Creá una cuenta gratis en github.com.
2. Arriba a la derecha: "+" -> "New repository". Nombre: mod67. Marcá "Public". "Create repository".
3. En la página del repo: "uploading an existing file". Arrastrá TODO el contenido de esta carpeta
   (incluida la carpeta .github) y apretá "Commit changes".
4. Si no ves la pestaña Actions corriendo, hacé: "Add file" -> "Create new file", poné el nombre
   .github/workflows/build.yml y pegá el contenido del archivo build.yml de esta carpeta.
5. Entrá a la pestaña "Actions". Cuando termine (tilde verde, unos 3-5 min), abrí la ejecución
   y abajo, en "Artifacts", bajá "mod67". Descomprimilo: adentro está el .jar.

## Instalar en el juego
1. Instalá Fabric Loader para 1.21.1 (fabricmc.net/use/installer).
2. Bajá Fabric API para 1.21.1 (modrinth.com/mod/fabric-api).
3. Poné en la carpeta mods (.minecraft/mods): el .jar de Mod 67 y el Fabric API.
4. Sacá el datapack y el resource pack viejos para que no se dupliquen.
