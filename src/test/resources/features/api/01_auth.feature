# language: es
@Auth
Característica: Autenticación y registro de personas en la plataforma
  Como persona que quiere usar la API de reservas
  Quiero iniciar sesión o crear una cuenta nueva
  Para obtener un token JWT que me permita acceder a los servicios protegidos

  Antecedentes:
    Dado que el sistema de autenticación está operativo

  Regla: el inicio de sesión solo devuelve un token con credenciales correctas

    @Login @Positivo
    Escenario: Inicio de sesión correcto devuelve un token Bearer
      Dado que existe una cuenta con correo "ana.perez@example.com" y clave "Valida123!"
      Cuando la persona inicia sesión en "/auth/login" con correo "ana.perez@example.com" y clave "Valida123!"
      Entonces el sistema responde con estado 200
      Y la respuesta contiene un token de tipo "Bearer"
      Y la respuesta identifica al usuario con correo "ana.perez@example.com"

    @Login @Negativo
    Escenario: Inicio de sesión con clave incorrecta es rechazado sin revelar qué dato falló
      Dado que existe una cuenta con correo "ana.perez@example.com" y clave "Valida123!"
      Cuando la persona inicia sesión en "/auth/login" con correo "ana.perez@example.com" y clave "ClaveIncorrecta9!"
      Entonces el sistema responde con estado 401
      Y el error es "Invalid credentials"
      Y no se devuelve ningún token

    @Login @Negativo
    Escenario: Inicio de sesión con correo no registrado devuelve el mismo error que una clave incorrecta
      Cuando la persona inicia sesión en "/auth/login" con correo "noexiste@example.com" y clave "Clave123!"
      Entonces el sistema responde con estado 401
      Y el error es "Invalid credentials"
      Y no se devuelve ningún token

    @Login @Validacion
    Esquema del escenario: Los campos obligatorios del inicio de sesión se validan antes de consultar la cuenta
      Cuando la persona inicia sesión en "/auth/login" con correo "<correo>" y clave "<clave>"
      Entonces el sistema responde con estado 400
      Y el detalle del error menciona "<campo>"

      Ejemplos:
        | correo                | clave      | campo    |
        |                       | Valida123! | email    |
        | correo-sin-arroba     | Valida123! | email    |
        | ana.perez@example.com |            | password |

  Regla: el registro crea una cuenta solo con datos válidos y correo único

    @Registro @Positivo
    Escenario: Registro correcto crea la cuenta con la clave cifrada y devuelve un token
      Dado los datos de registro de una persona nueva:
        | name      | lastName | email                    | password    | birthDate  |
        | Alejandro | Morales  | alex.morales@example.com | Secreta123* | 1995-05-15 |
      Cuando la persona se registra en "/auth/register" con los datos de registro
      Entonces el sistema responde con estado 201
      Y la respuesta contiene un token y el uuid de la cuenta creada
      Y existe una cuenta guardada con correo "alex.morales@example.com"
      Y la clave guardada está cifrada y no coincide con la original

    @Registro @Negativo @Duplicado
    Escenario: Registro con un correo que ya tiene cuenta es rechazado
      Dado que ya existe una cuenta con correo "existente@example.com"
      Cuando la persona se registra en "/auth/register" con correo "existente@example.com" y clave "Valida123!"
      Entonces el sistema responde con estado 409
      Y el detalle del error menciona "Email is already registered"

    @Registro @Validacion
    Esquema del escenario: La clave debe cumplir la política de complejidad al registrarse
      Cuando la persona se registra en "/auth/register" con clave "<clave>" y resto de datos válidos
      Entonces el sistema responde con estado 400
      Y el detalle del error menciona "password"

      Ejemplos:
        | clave                                   | motivo                                    |
        | Corta1!                                 | Menos de 8 caracteres                     |
        | solominusculas                          | Sin mayúsculas, números ni símbolos       |
        | SOLOMAYUSCULAS                          | Sin minúsculas ni números                 |
        | SoloLetrasSinN                          | Sin números ni caracteres especiales      |
        | SinCaracter123                          | Sin caracteres especiales                 |
        | EstaPasswordEsDemasiadoLarga123456789!  | Supera los 20 caracteres                  |

    @Registro @Validacion
    Escenario: La fecha de nacimiento debe ser una fecha pasada
      Cuando la persona se registra en "/auth/register" con fecha de nacimiento "2099-01-01"
      Entonces el sistema responde con estado 400
      Y el detalle del error menciona "birthDate"
