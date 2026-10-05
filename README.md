# Spring Template API

Plantilla reutilizable para pruebas técnicas con una arquitectura monolítica por capas: controller, service, repository, mapper, DTOs, seguridad JWT y manejo global de errores.

La plantilla deja el dominio de negocio abierto, pero establece decisiones reutilizables: autenticación stateless, contraseñas con BCrypt, autorización por rol, respuestas sin datos sensibles, paginación y separación entre configuración de desarrollo y producción.

## Cómo funciona

La API administra usuarios y expone un flujo de autenticación basado en JWT:

1. Al iniciar, `DataInitializer` crea el administrador inicial si aún no existe.
2. El cliente inicia sesión en `POST /api/auth/login` con su correo y contraseña.
3. La API valida la contraseña mediante BCrypt y devuelve un JWT firmado con el correo y el rol del usuario.
4. El cliente envía ese JWT en `Authorization: Bearer <token>` para acceder a las rutas protegidas.
5. Spring Security valida la firma y transforma el claim `role` del token en una autoridad. Cualquier usuario autenticado puede consultar usuarios; únicamente `ADMIN` puede crear, editar o eliminar.

Las capas están separadas para que puedas sustituir el dominio sin acoplar HTTP, persistencia y lógica de negocio: los controladores reciben solicitudes, los servicios aplican las reglas, los repositorios acceden a JPA/H2 y los DTOs evitan exponer la entidad o el hash de la contraseña.

## Requisitos

- Java 21
- Maven Wrapper incluido

Comprueba que Java esté disponible:

```bash
java -version
```

No necesitas instalar Maven de forma global: los comandos usan el Maven Wrapper incluido.

## Configuración

La configuración principal está en `src/main/resources/application.yml`. Por defecto se activa el perfil `dev`, que utiliza una base de datos H2 en memoria y escucha en el puerto `8080`.

| Propiedad | Desarrollo (valor por defecto) | Para qué sirve |
| --- | --- | --- |
| `spring.datasource.url` | `jdbc:h2:mem:template_db` | Conexión a la base de datos. |
| `server.port` | `8080` | Puerto HTTP de la API. |
| `application.security.jwt.secret-key` | Clave de desarrollo incluida | Firma y validación de JWT. |
| `application.security.jwt.expiration` | `3600000` | Duración del token en milisegundos (1 hora). |
| `application.security.jwt.issuer` | `spring-template-api` | Emisor incluido en los JWT. |

Para cambiar valores sin editar archivos, define variables de entorno. Por ejemplo, para usar otro puerto y una clave JWT propia en desarrollo:

```bash
SERVER_PORT=8081 \
JWT_SECRET_KEY='una-clave-secreta-de-al-menos-32-caracteres' \
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
$env:SERVER_PORT = '8081'
$env:JWT_SECRET_KEY = 'una-clave-secreta-de-al-menos-32-caracteres'
.\mvnw.cmd spring-boot:run
```

No publiques claves reales en `application.yml`, en commits ni en el README. Para producción, proporciona `JWT_SECRET_KEY` desde las variables de entorno o el gestor de secretos de tu plataforma.

## Adaptar esta plantilla a un proyecto nuevo

Sigue estos pasos antes de empezar a desarrollar el nuevo dominio. El objetivo es conservar la estructura reutilizable (seguridad, errores, DTOs y configuración) y reemplazar el ejemplo de usuarios por las entidades y reglas del proyecto.

### 1. Renombra el proyecto Maven

Edita `pom.xml` y cambia los identificadores del proyecto:

```xml
<groupId>com.tuempresa</groupId>
<artifactId>mi-nuevo-proyecto</artifactId>
<version>0.0.1-SNAPSHOT</version>
<name>mi-nuevo-proyecto</name>
<description>Descripción breve del proyecto</description>
```

El `artifactId` determina el nombre del archivo JAR generado. Actualiza también `spring.application.name` en `src/main/resources/application.yml` y el valor de `application.security.jwt.issuer` para que identifiquen al nuevo servicio.

### 2. Cambia el paquete base

El paquete actual es `com.alberto.springtemplate`. Sustitúyelo por uno propio, por ejemplo `com.tuempresa.miproyecto`.

1. Renombra la estructura de carpetas desde `src/main/java/com/alberto/springtemplate` y `src/test/java/com/alberto/springtemplate` hasta la nueva ruta.
2. Reemplaza las sentencias `package` e `import` que contengan `com.alberto.springtemplate` en código principal y pruebas.
3. Renombra la clase `SpringTemplateApplication` y su archivo, por ejemplo a `MiProyectoApplication`.
4. Confirma que la clase principal permanezca en el paquete raíz; Spring Boot detectará automáticamente los componentes de sus subpaquetes.

Puedes localizar las referencias pendientes con:

```bash
rg 'com\.alberto\.springtemplate|SpringTemplateApplication' src pom.xml
```

### 3. Ajusta la versión de Java y las dependencias

La plantilla usa Java 21. Si tu entorno requiere otra versión compatible con la versión de Spring Boot elegida, modifica esta propiedad en `pom.xml`:

```xml
<properties>
    <java.version>21</java.version>
</properties>
```

Después actualiza el JDK del IDE, `JAVA_HOME` y la configuración de compilación/CI para que todos usen la misma versión. Verifica el cambio con `./mvnw clean test`.

Agrega o elimina dependencias según el proyecto: por ejemplo, un driver de PostgreSQL/MySQL para producción, clientes HTTP, mensajería o caché. Mantén únicamente las dependencias que use el servicio y ejecuta las pruebas tras cada cambio.

### 4. Configura la base de datos por entorno

H2 está pensada para desarrollo local. Para utilizar una base de datos real:

1. Agrega al `pom.xml` el driver JDBC de la base de datos elegida.
2. Define `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` y, si hace falta, `spring.datasource.driver-class-name` mediante variables de entorno o un archivo de perfil que no contenga secretos.
3. Decide la estrategia de esquema (`ddl-auto` para prototipos o migraciones con Flyway/Liquibase para entornos persistentes).
4. Desactiva o elimina `DataInitializer` si no debe crear el usuario administrador por defecto.

No uses `create-drop` en una base de datos con información que deba conservarse.

### 5. Reemplaza el dominio de ejemplo

El módulo actual de usuarios sirve como referencia. Para cada entidad del nuevo dominio (por ejemplo, `Order`, `Product` o `Customer`), crea y conecta:

1. La entidad JPA en `entity/`, con sus relaciones y restricciones.
2. El repositorio en `repository/`.
3. DTOs de entrada y salida en `dto/request` y `dto/response`, con validaciones de `jakarta.validation`.
4. El mapper, interfaz de servicio e implementación.
5. El controlador REST, con rutas, códigos HTTP y paginación cuando corresponda.
6. Pruebas de repositorio, servicio y controlador equivalentes a las existentes.

Evita devolver entidades JPA directamente desde los controladores: los DTOs permiten controlar los datos que entran y salen de la API.

### 6. Decide el modelo de seguridad

La seguridad JWT actual puede conservarse tal como está si el proyecto necesita usuarios con roles. En ese caso, cambia o amplía el enum `Role` y las reglas `@PreAuthorize` de los controladores.

Si el servicio no necesita autenticación, elimina o adapta `SecurityConfig`, `JwtService`, `AuthController`, `AuthService` y sus DTOs. Si requiere un proveedor de identidad externo, sustituye la generación local de tokens y configura el resource server con el emisor o las claves públicas del proveedor.

Al conservar JWT, establece siempre `JWT_SECRET_KEY` con una clave privada robusta fuera del repositorio y revisa la duración definida en `JWT_EXPIRATION`.

### 7. Actualiza documentación y verificación

1. Sustituye en este README el nombre, objetivo, endpoints, credenciales de ejemplo y variables de configuración.
2. Actualiza la información de OpenAPI en `config/OpenApiConfig.java`.
3. Elimina las credenciales del administrador inicial de la documentación si `DataInitializer` fue eliminado o modificado.
4. Ejecuta `./mvnw clean test` y prueba los endpoints desde Swagger antes de publicar.

Al terminar, busca referencias que hayan quedado de la plantilla:

```bash
rg -n -i 'spring-template|springtemplate|admin@example\.com|Admin123' . \
  -g '!target' -g '!.git'
```

## Repaso rápido para entrevista técnica (Java)

Esta sección no forma parte del comportamiento de la API; es una referencia breve de patrones habituales en ejercicios de razonamiento. Antes de usar `Stream`, explica la complejidad esperada y elige la versión más clara para el problema.

### Bucles anidados y `Stream`

Un bucle dentro de otro se utiliza para recorrer pares, comparar elementos o aplanar una colección de colecciones. Si cada lista tiene `n` elementos, dos bucles completos suelen costar `O(n²)`; si tienen tamaños distintos, el costo es `O(n × m)`.

**Generar todos los pares de dos listas:**

```java
List<Integer> left = List.of(1, 2);
List<Integer> right = List.of(10, 20);
List<String> pairs = new ArrayList<>();

for (Integer a : left) {
    for (Integer b : right) {
        pairs.add(a + ":" + b);
    }
}
// ["1:10", "1:20", "2:10", "2:20"]
```

El equivalente más común con `Stream` usa `flatMap`: por cada elemento externo crea un `Stream` interno y lo aplana en un único resultado.

```java
List<String> pairs = left.stream()
        .flatMap(a -> right.stream().map(b -> a + ":" + b))
        .toList();
```

**Aplanar una lista de listas:**

```java
List<List<Integer>> groups = List.of(List.of(1, 2), List.of(3, 4));

List<Integer> values = groups.stream()
        .flatMap(List::stream)
        .toList();
// [1, 2, 3, 4]
```

**Buscar pares cuya suma sea un objetivo:**

```java
int[] numbers = {2, 7, 11, 15};
int target = 9;

for (int i = 0; i < numbers.length; i++) {
    for (int j = i + 1; j < numbers.length; j++) {
        if (numbers[i] + numbers[j] == target) {
            System.out.println(i + ", " + j);
        }
    }
}
```

Esta versión evita pares repetidos con `j = i + 1`, pero sigue siendo `O(n²)`. Para el ejercicio clásico *Two Sum*, un `HashMap` reduce el tiempo a `O(n)` promedio:

```java
Map<Integer, Integer> indexByValue = new HashMap<>();
for (int i = 0; i < numbers.length; i++) {
    int needed = target - numbers[i];
    if (indexByValue.containsKey(needed)) {
        System.out.println(indexByValue.get(needed) + ", " + i);
        break;
    }
    indexByValue.put(numbers[i], i);
}
```

### Operaciones de `Stream` más usadas

Un `Stream` es una secuencia para transformar o consultar datos; no modifica la colección original. Las operaciones intermedias crean el flujo y las terminales lo ejecutan.

| Operación | Uso | Ejemplo |
| --- | --- | --- |
| `filter` | Conserva elementos que cumplen una condición. | `users.stream().filter(User::active)` |
| `map` | Convierte cada elemento a otro valor. | `users.stream().map(User::name)` |
| `flatMap` | Aplana un `Stream` por cada elemento. | `groups.stream().flatMap(List::stream)` |
| `sorted` | Ordena; usa `Comparator` para objetos. | `users.stream().sorted(comparing(User::name))` |
| `distinct` | Elimina duplicados según `equals`/`hashCode`. | `numbers.stream().distinct()` |
| `limit` / `skip` | Toma u omite una cantidad de elementos. | `stream.skip(20).limit(20)` |
| `anyMatch` / `allMatch` / `noneMatch` | Comprueba si alguno, todos o ninguno cumplen. | `values.stream().anyMatch(v -> v < 0)` |
| `findFirst` | Obtiene el primer resultado como `Optional`. | `stream.filter(...).findFirst()` |
| `count` | Cuenta elementos. | `stream.filter(...).count()` |
| `reduce` | Combina elementos en un único valor. | `numbers.stream().reduce(0, Integer::sum)` |
| `collect(groupingBy(...))` | Agrupa elementos por una clave. | `users.stream().collect(groupingBy(User::role))` |
| `toList` | Materializa el resultado en una lista. | `stream.map(...).toList()` |

No reutilices un `Stream` después de una operación terminal como `toList`, `count` o `findFirst`: crea uno nuevo desde la colección. Evita `parallelStream()` salvo que hayas medido un beneficio y las operaciones sean seguras para concurrencia.

### Texto y `String` más usados

`String` es inmutable: cada transformación devuelve un objeto nuevo. Para concatenaciones repetidas dentro de un ciclo, usa `StringBuilder`.

| Método | Descripción breve | Ejemplo |
| --- | --- | --- |
| `length()` | Número de caracteres. | `text.length()` |
| `isEmpty()` / `isBlank()` | Comprueba si está vacío; `isBlank` también acepta espacios. | `text.isBlank()` |
| `charAt(i)` | Carácter en un índice. | `text.charAt(0)` |
| `substring(inicio, fin)` | Extrae desde inicio (incluido) hasta fin (excluido). | `text.substring(0, 3)` |
| `contains`, `startsWith`, `endsWith` | Busca una secuencia o prefijo/sufijo. | `email.endsWith(".com")` |
| `indexOf` / `lastIndexOf` | Posición de una coincidencia; `-1` si no existe. | `text.indexOf('a')` |
| `equals` / `equalsIgnoreCase` | Compara contenido, nunca uses `==` para texto. | `a.equals(b)` |
| `toLowerCase` / `toUpperCase` | Cambia mayúsculas/minúsculas; usa `Locale.ROOT` en datos técnicos. | `text.toLowerCase(Locale.ROOT)` |
| `trim` / `strip` | Quita espacios extremos; `strip` reconoce Unicode. | `text.strip()` |
| `replace` | Reemplazo literal. | `text.replace("-", "")` |
| `replaceAll` | Reemplazo con expresión regular. | `text.replaceAll("\\s+", " ")` |
| `split` | Divide usando una expresión regular. | `text.split(",")` |
| `String.join` | Une valores con un separador. | `String.join(", ", names)` |

Ejemplo para invertir una cadena, frecuente en ejercicios:

```java
String reversed = new StringBuilder(text).reverse().toString();
```

### Colecciones y complejidad que conviene recordar

| Estructura | Úsala cuando necesites | Operaciones típicas |
| --- | --- | --- |
| `ArrayList` | Acceso por índice y recorrido. | `get(i)` `O(1)`; insertar al final amortizado `O(1)`. |
| `HashSet` | Valores únicos y consultas rápidas. | `add`/`contains` promedio `O(1)`. |
| `HashMap` | Asociar una clave a un valor. | `put`/`get` promedio `O(1)`. |
| `ArrayDeque` | Pila o cola eficiente. | Agregar/quitar en extremos `O(1)`. |
| `TreeMap` / `TreeSet` | Datos ordenados por clave. | Inserción y búsqueda `O(log n)`. |

En entrevistas, verbaliza casos límite antes de programar: colección vacía, un único elemento, duplicados, valores negativos, `null`, límites de índices y desbordamiento numérico. Después indica la complejidad temporal y espacial de tu solución.

## Ejecutar

Linux:

```bash
./mvnw spring-boot:run
./mvnw test
./mvnw clean test
```

Windows:

```bat
mvnw.cmd spring-boot:run
mvnw.cmd test
mvnw.cmd clean test
```

## Usuario administrador inicial

- Email: `admin@example.com`
- Password: `Admin123!`

## Endpoints

| Método | Ruta | Autenticación |
| --- | --- | --- |
| POST | `/api/auth/login` | Pública |
| GET | `/api/users` | Bearer JWT |
| GET | `/api/users/{id}` | Bearer JWT |
| POST | `/api/users` | Bearer JWT |
| PUT | `/api/users/{id}` | Bearer JWT |
| DELETE | `/api/users/{id}` | Bearer JWT |

Las consultas requieren un usuario autenticado. Crear, actualizar y eliminar usuarios requiere el rol `ADMIN`; el claim `role` del JWT se convierte a autoridades de Spring Security.

Ejemplo de login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"Admin123!"}'
```

Usa el token devuelto en las rutas protegidas:

```text
Authorization: Bearer <token>
```

## Herramientas locales

- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:template_db`
  - Usuario: `sa`
  - Password: vacío
- Swagger UI: http://localhost:8080/swagger-ui.html

Swagger incluye el botón **Authorize**. Pega únicamente el JWT; la interfaz añade el prefijo `Bearer`.

## Perfiles y secretos

El perfil por defecto es desarrollo y usa H2 en memoria. Para producción activa el perfil `prod`; H2 Console y SQL detallado se deshabilitan y `JWT_SECRET_KEY` es obligatoria.

```bash
SPRING_PROFILES_ACTIVE=prod JWT_SECRET_KEY='una-clave-secreta-de-al-menos-32-caracteres' ./mvnw spring-boot:run
```

El perfil `prod` está definido en `src/main/resources/application-prod.yml`. Antes de desplegar, configura también una fuente de datos adecuada para producción (por ejemplo, la URL, usuario, contraseña y driver de PostgreSQL o MySQL) y agrega la dependencia JDBC correspondiente en `pom.xml`; la configuración actual usa H2 para desarrollo.

`POST /api/users` asigna el rol `USER`. Al actualizar un usuario, `password` es opcional: si no se envía, la contraseña existente se conserva.
