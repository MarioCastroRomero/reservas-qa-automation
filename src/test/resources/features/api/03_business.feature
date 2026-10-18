# language: es
Característica: gestión de empresas /business
  Como usuario de la API de reservas
  quiero consultar y gestionar las empresas
  para poder gestionar las empresas de la plataforma

  Antecedentes:
    Dado que el servicio de empresas está disponible

  @Business @Lectura @Busqueda @Positivo
  Esquema del escenario: Cualquier rol puede listar las empresas activas
    Dado el catálogo de empresas de ejemplo
    Y una petición autenticada con rol "<rol>"
    Cuando se envía una petición GET a "/business"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe contener exactamente las empresas "Barbería Centro, Barbería Centro REC, Clínica Dental Luna, Clínica Dental Sol, Peluquería Ana, Peluquería PEEP"

    Ejemplos:
      | rol            |
      | USER           |
      | BUSINESS_OWNER |
      | ADMIN          |

  @Business @Lectura @Busqueda @Positivo
  Esquema del escenario: Búsqueda de empresas activas por texto
    Dado el catálogo de empresas de ejemplo
    Y una petición autenticada con rol "USER"
    Y el parámetro "q" con valor "<texto>"
    Cuando se envía una petición GET a "/business"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe contener exactamente las empresas "<esperadas>"

    Ejemplos:
      | texto      | esperadas                                                                |
      | peluquería | Peluquería Ana, Peluquería PEEP                                          |
      | PELUQUERÍA | Peluquería Ana, Peluquería PEEP                                          |
      | madrid     | Clínica Dental Luna, Clínica Dental Sol, Peluquería Ana, Peluquería PEEP |
      | sevilla    | Barbería Centro, Barbería Centro REC                                     |
      | avenida    | Clínica Dental Luna, Clínica Dental Sol                                  |
      | calle mayor | Peluquería Ana, Peluquería PEEP                                         |

  @Business @Lectura @Busqueda @Positivo
  Esquema del escenario: Paginación y orden del listado de empresas
    Dado el catálogo de empresas de ejemplo
    Y una petición autenticada con rol "USER"
    Y el parámetro "page" con valor "<page>"
    Y el parámetro "size" con valor "2"
    Y el parámetro "sort" con valor "<sort>"
    Cuando se envía una petición GET a "/business"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe contener las empresas en este orden "<esperadas>"

    Ejemplos:
      | page | sort      | esperadas                                |
      | 0    | name,asc  | Barbería Centro, Barbería Centro REC     |
      | 1    | name,asc  | Clínica Dental Luna, Clínica Dental Sol  |
      | 2    | name,asc  | Peluquería Ana, Peluquería PEEP          |
      | 0    | name,desc | Peluquería PEEP, Peluquería Ana          |

  @Business @Lectura @Busqueda @Negativo
  Esquema del escenario: Búsqueda que no devuelve empresas
    Dado el catálogo de empresas de ejemplo
    Y una petición autenticada con rol "USER"
    Y el parámetro "q" con valor "<texto>"
    Cuando se envía una petición GET a "/business"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe ser una lista vacía

    Ejemplos:
      | texto       |
      | zapatería   |
      | Cerrada     |
      | ' OR '1'='1 |
      | cadiz       |
      | %           |
      | _           |

  @Business @Lectura @Busqueda @Seguridad @Negativo
  Escenario: Búsqueda sin token de autorización
    Dado sin cabecera "Authorization"
    Cuando se envía una petición GET a "/business"
    Entonces la respuesta tiene el estado 401
    Y la respuesta es un error de autenticación

  @Business @Lectura @Busqueda @Validacion @Negativo
  Escenario: Búsqueda ordenando por un campo inexistente
    Dado una petición autenticada con rol "USER"
    Y el parámetro "sort" con valor "campoInventado"
    Cuando se envía una petición GET a "/business"
    Entonces la respuesta tiene el estado 400
    Y el error debe indicar "Validation error"

  @Business @Lectura @Consulta @Positivo
  Esquema del escenario: consulta de una empresa activa
    Dado una petición autenticada con rol "<rol>"
    Y una empresa activa cuyo dueño es "otro"
    Cuando se envía una petición GET a "/business/{uuid}"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe contener la empresa solicitada

    Ejemplos:
      | rol            |
      | USER           |
      | BUSINESS_OWNER |
      | ADMIN          |

  @Business @Lectura @Consulta @Negativo
  Escenario: consulta de una empresa inactiva
    Dado una petición autenticada con rol "USER"
    Y una empresa inactiva cuyo dueño es "otro"
    Cuando se envía una petición GET a "/business/{uuid}"
    Entonces la respuesta tiene el estado 404
    Y el details debe contener "Business not found"
    Y el error debe indicar "Resource not found"

  @Business @Lectura @Consulta @Seguridad @Negativo
  Escenario: Consulta empresa activa sin token de autorización
    Dado una empresa activa cuyo dueño es "otro"
    Y sin cabecera "Authorization"
    Cuando se envía una petición GET a "/business/{uuid}"
    Entonces la respuesta tiene el estado 401
    Y la respuesta es un error de autenticación

  @Business @Lectura @Consulta @Negativo
  Escenario: consulta de una empresa que no existe
    Dado una petición autenticada con rol "USER"
    Y una empresa inexistente con UUID "00000000-0000-0000-0000-000000000000"
    Cuando se envía una petición GET a "/business/{uuid}"
    Entonces la respuesta tiene el estado 404
    Y el details debe contener "Business not found"
    Y el error debe indicar "Resource not found"

  @Business @Lectura @Consulta @Validacion @Negativo
  Escenario: consulta de una empresa con UUID inválido
    Dado una petición autenticada con rol "USER"
    Y una empresa inexistente con UUID "uuid-no-valido-123"
    Cuando se envía una petición GET a "/business/{uuid}"
    Entonces la respuesta tiene el estado 400
    Y el details debe contener "Invalid UUID"
    Y el error debe indicar "Validation error"

  @Business @Lectura @Consulta @Positivo
  Esquema del escenario: Consulta de mis empresas según el rol
    Dado el catálogo de empresas de ejemplo
    Y una petición autenticada con rol "<rol>"
    Y una empresa activa cuyo dueño es "yo"
    Cuando se envía una petición GET a "/business/mine"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe contener las empresas pertenecientes del usuario

    Ejemplos:
      | rol            |
      | BUSINESS_OWNER |
      | ADMIN          |

  @Business @Lectura @Consulta @Positivo
  Escenario: Consulta de mis empresas no incluye las inactivas ni las de otros dueños
    Dado el catálogo de empresas de ejemplo
    Y una petición autenticada con rol "BUSINESS_OWNER"
    Cuando se envía una petición GET a "/business/mine"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe contener exactamente las empresas "Barbería Centro, Clínica Dental Sol, Peluquería Ana"

  @Business @Lectura @Consulta @Seguridad @Negativo
  Escenario: Consulta de mis empresas sin token de autorización
    Dado sin cabecera "Authorization"
    Cuando se envía una petición GET a "/business/mine"
    Entonces la respuesta tiene el estado 401
    Y la respuesta es un error de autenticación

  @Business @Lectura @Consulta @Negativo
  Escenario: Consulta de mis empresas con rol usuario
    Dado una petición autenticada con rol "USER"
    Cuando se envía una petición GET a "/business/mine"
    Entonces la respuesta tiene el estado 403
    Y el details debe contener "You do not have permission to perform this action"
    Y el error debe indicar "Forbidden"

  @Business @Lectura @Consulta @Validacion @Negativo
  Esquema del escenario: Consulta de mis empresas ordenando por un campo inexistente
    Dado una petición autenticada con rol "<rol>"
    Y el parámetro "sort" con valor "campoInventado"
    Cuando se envía una petición GET a "/business/mine"
    Entonces la respuesta tiene el estado 400
    Y el error debe indicar "Validation error"

    Ejemplos:
      | rol            |
      | BUSINESS_OWNER |
      | ADMIN          |

  @Business @Guardado @Positivo
  Esquema del escenario: crea una empresa con rol empresa o admin
    Dado una petición autenticada con rol "<rol>"
    Y los datos de una empresa válida
    Cuando se envía una petición POST a "/business"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe contener la empresa
    Y mi rol debe seguir siendo el mismo

    Ejemplos:
      | rol            |
      | BUSINESS_OWNER |
      | ADMIN          |

  @Business @Guardado @Positivo
  Escenario: crea una empresa con rol de usuario
    Dado una petición autenticada con rol "USER"
    Y los datos de una empresa válida
    Cuando se envía una petición POST a "/business"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe contener la empresa
    Y mi rol debe haber cambiado a "BUSINESS_OWNER"

  @Business @Guardado @Negativo
  Escenario: crea una empresa ya existente
    Dado una petición autenticada con rol "USER"
    Y los datos de una empresa con un CIF ya registrado
    Cuando se envía una petición POST a "/business"
    Entonces la respuesta tiene el estado 409
    Y el details debe contener "CIF already exists"
    Y el error debe indicar "Conflict"

  @Business @Guardado @Validacion @Negativo
  Escenario: crea una empresa con un CIF no valido
    Dado una petición autenticada con rol "USER"
    Y los datos de una empresa con un CIF "1234B5679"
    Cuando se envía una petición POST a "/business"
    Entonces la respuesta tiene el estado 400
    Y el details debe contener "cif: CIF format is not valid (example: B12345678 or A1234567J)"
    Y el error debe indicar "Validation error"

  @Business @Guardado @Validacion @Negativo
  Esquema del escenario: crea una empresa con un campo inválido
    Dado una petición autenticada con rol "USER"
    Y los datos de una empresa válida con "<campo>" igual a "<valor>"
    Cuando se envía una petición POST a "/business"
    Entonces la respuesta tiene el estado 400
    Y el details debe contener "<mensaje>"
    Y el error debe indicar "Validation error"

    Ejemplos:
      | campo   | valor | mensaje                                                                          |
      | name    |       | name: Name must not be blank                                                     |
      | address |       | address: Address must not be blank                                               |
      | address | Ab    | address: Address must be between 5 and 150 characters and only contain valid characters |
      | address | Calle $ 5 | address: Address must be between 5 and 150 characters and only contain valid characters |
      | cif     |       | cif: CIF must not be blank                                                       |

  @Business @Guardado @Validacion @Negativo
  Escenario: crea una empresa con un nombre demasiado largo
    Dado una petición autenticada con rol "USER"
    Y los datos de una empresa con un nombre de 101 caracteres
    Cuando se envía una petición POST a "/business"
    Entonces la respuesta tiene el estado 400
    Y el details debe contener "name: Name must be at most 100 characters"
    Y el error debe indicar "Validation error"

  @Business @Guardado @Validacion @Negativo
  Escenario: crea una empresa con un cuerpo vacío
    Dado una petición autenticada con rol "USER"
    Y los datos de una empresa vacíos
    Cuando se envía una petición POST a "/business"
    Entonces la respuesta tiene el estado 400
    Y el details debe contener "name: Name must not be blank"
    Y el details debe contener "cif: CIF must not be blank"
    Y el details debe contener "address: Address must not be blank"
    Y el error debe indicar "Validation error"

  @Business @Guardado @Validacion @Negativo
  Escenario: crea una empresa sin cuerpo
    Dado una petición autenticada con rol "USER"
    Cuando se envía una petición POST a "/business"
    Entonces la respuesta tiene el estado 400
    Y el details debe contener "Malformed or unreadable request body"
    Y el error debe indicar "Validation error"

  @Business @Guardado @Seguridad @Negativo
  Escenario: crea una empresa sin token de autorización
    Dado los datos de una empresa válida
    Y sin cabecera "Authorization"
    Cuando se envía una petición POST a "/business"
    Entonces la respuesta tiene el estado 401
    Y la respuesta es un error de autenticación

  @Business @Actualizar @Positivo
  Esquema del escenario: actualiza una empresa con permisos
    Dado una petición autenticada con rol "<rol>"
    Y una empresa activa cuyo dueño es "<dueño>"
    Y los datos de una empresa válida
    Cuando se envía una petición PUT a "/business/{uuid}"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe contener la empresa

    Ejemplos:
      | rol            | dueño |
      | ADMIN          | otro  |
      | BUSINESS_OWNER | yo    |

  @Business @Actualizar @Negativo
  Esquema del escenario: actualiza una empresa sin permisos
    Dado una petición autenticada con rol "<rol>"
    Y una empresa activa cuyo dueño es "otro"
    Y los datos de una empresa válida
    Cuando se envía una petición PUT a "/business/{uuid}"
    Entonces la respuesta tiene el estado 403
    Y el details debe contener "<mensaje>"
    Y el error debe indicar "Forbidden"

    Ejemplos:
      | rol            | mensaje                                           |
      | BUSINESS_OWNER | You are not the owner of this business            |
      | USER           | You do not have permission to perform this action |

  @Business @Actualizar @Negativo
  Escenario: actualiza una empresa que no existe
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa inexistente con UUID "00000000-0000-0000-0000-000000000000"
    Y los datos de una empresa válida
    Cuando se envía una petición PUT a "/business/{uuid}"
    Entonces la respuesta tiene el estado 404
    Y el details debe contener "Business not found"
    Y el error debe indicar "Resource not found"

  @Business @Actualizar @Validacion @Negativo
  Escenario: actualiza una empresa que no existe con UUID invalido
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa inexistente con UUID "uuid-no-valido-123"
    Y los datos de una empresa válida
    Cuando se envía una petición PUT a "/business/{uuid}"
    Entonces la respuesta tiene el estado 400
    Y el error debe indicar "Validation error"

  @Business @Actualizar @Negativo
  Escenario: actualiza una empresa con el CIF de otra empresa
    Dado una petición autenticada con rol "ADMIN"
    Y una empresa activa cuyo dueño es "otro"
    Y los datos de una empresa con un CIF ya registrado
    Cuando se envía una petición PUT a "/business/{uuid}"
    Entonces la respuesta tiene el estado 409
    Y el details debe contener "CIF already exists"
    Y el error debe indicar "Conflict"

  @Business @Actualizar @Validacion @Negativo
  Esquema del escenario: actualiza una empresa con un campo inválido
    Dado una petición autenticada con rol "ADMIN"
    Y una empresa activa cuyo dueño es "otro"
    Y los datos de una empresa válida con "<campo>" igual a "<valor>"
    Cuando se envía una petición PUT a "/business/{uuid}"
    Entonces la respuesta tiene el estado 400
    Y el details debe contener "<mensaje>"
    Y el error debe indicar "Validation error"

    Ejemplos:
      | campo   | valor     | mensaje                                                                                  |
      | name    |           | name: Name must not be blank                                                             |
      | cif     | 1234B5679 | cif: CIF format is not valid (example: B12345678 or A1234567J)                           |
      | address | Ab        | address: Address must be between 5 and 150 characters and only contain valid characters |

  @Business @Actualizar @Seguridad @Negativo
  Escenario: actualiza una empresa sin token de autorización
    Dado una empresa activa cuyo dueño es "otro"
    Y los datos de una empresa válida
    Y sin cabecera "Authorization"
    Cuando se envía una petición PUT a "/business/{uuid}"
    Entonces la respuesta tiene el estado 401
    Y la respuesta es un error de autenticación

  @Business @Borrado @Positivo
  Esquema del escenario: borra una empresa con permisos
    Dado una petición autenticada con rol "<rol>"
    Y una empresa activa cuyo dueño es "<dueño>"
    Cuando se envía una petición DELETE a "/business/{uuid}"
    Entonces la respuesta tiene el estado 204
    Y la respuesta debe estar vacia

    Ejemplos:
      | rol            | dueño |
      | ADMIN          | otro  |
      | BUSINESS_OWNER | yo    |

  @Business @Borrado @Negativo
  Esquema del escenario: borra una empresa sin permisos
    Dado una petición autenticada con rol "<rol>"
    Y una empresa activa cuyo dueño es "otro"
    Cuando se envía una petición DELETE a "/business/{uuid}"
    Entonces la respuesta tiene el estado 403
    Y el details debe contener "<mensaje>"
    Y el error debe indicar "Forbidden"

    Ejemplos:
      | rol            | mensaje                                           |
      | BUSINESS_OWNER | You are not the owner of this business            |
      | USER           | You do not have permission to perform this action |

  @Business @Borrado @Negativo
  Escenario: borra una empresa que no existe
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa inexistente con UUID "00000000-0000-0000-0000-000000000000"
    Cuando se envía una petición DELETE a "/business/{uuid}"
    Entonces la respuesta tiene el estado 404
    Y el details debe contener "Business not found"
    Y el error debe indicar "Resource not found"

  @Business @Borrado @Negativo
  Escenario: borra una empresa de la que es dueño pero tiene reservas activas
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa activa cuyo dueño es "yo"
    Y que tiene reservas activas
    Cuando se envía una petición DELETE a "/business/{uuid}"
    Entonces la respuesta tiene el estado 409
    Y el details debe contener "Business has pending or confirmed bookings"
    Y el error debe indicar "Conflict"

  @Business @Borrado @Positivo
  Escenario: borrar una empresa la deja inactiva
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa activa cuyo dueño es "yo"
    Cuando se envía una petición DELETE a "/business/{uuid}"
    Entonces la respuesta tiene el estado 204
    Y la empresa debe haber quedado inactiva

  @Business @Borrado @Validacion @Negativo
  Escenario: borra una empresa con UUID inválido
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa inexistente con UUID "uuid-no-valido-123"
    Cuando se envía una petición DELETE a "/business/{uuid}"
    Entonces la respuesta tiene el estado 400
    Y el details debe contener "Invalid UUID"
    Y el error debe indicar "Validation error"

  @Business @Borrado @Seguridad @Negativo
  Escenario: borra una empresa sin token de autorización
    Dado una empresa activa cuyo dueño es "otro"
    Y sin cabecera "Authorization"
    Cuando se envía una petición DELETE a "/business/{uuid}"
    Entonces la respuesta tiene el estado 401
    Y la respuesta es un error de autenticación

  @Business @Lectura @Bookings @Consulta @Positivo
  Esquema del escenario: consulta todas las reservas de una empresa
    Dado una petición autenticada con rol "<rol>"
    Y una empresa activa cuyo dueño es "<dueño>"
    Y que tiene reservas
    Cuando se envía una petición GET a "/business/{uuid}/bookings"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe contener todas las reservas de la empresa

    Ejemplos:
      | rol            | dueño |
      | ADMIN          | otro  |
      | BUSINESS_OWNER | yo    |

  @Business @Lectura @Bookings @Consulta @Positivo
  Esquema del escenario: consulta las reservas de una empresa filtradas por status
    Dado una petición autenticada con rol "<rol>"
    Y una empresa activa cuyo dueño es "<dueño>"
    Y que tiene reservas
    Y el parámetro "status" con valor "<status>"
    Cuando se envía una petición GET a "/business/{uuid}/bookings"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe contener todas las reservas de la empresa con status "<status>"

    Ejemplos:
      | rol            | dueño | status    |
      | ADMIN          | otro  | PENDING   |
      | ADMIN          | otro  | CONFIRMED |
      | ADMIN          | otro  | CANCELLED |
      | ADMIN          | otro  | COMPLETED |
      | BUSINESS_OWNER | yo    | PENDING   |
      | BUSINESS_OWNER | yo    | CONFIRMED |
      | BUSINESS_OWNER | yo    | CANCELLED |
      | BUSINESS_OWNER | yo    | COMPLETED |

  @Business @Lectura @Bookings @Consulta @Negativo
  Escenario: consulta las reservas de una empresa como usuario
    Dado una petición autenticada con rol "USER"
    Y una empresa activa cuyo dueño es "otro"
    Y que tiene reservas
    Cuando se envía una petición GET a "/business/{uuid}/bookings"
    Entonces la respuesta tiene el estado 403
    Y el details debe contener "You do not have permission to perform this action"
    Y el error debe indicar "Forbidden"

  @Business @Lectura @Bookings @Consulta @Seguridad @Negativo
  Escenario: consulta las reservas de una empresa sin token
    Dado una empresa activa cuyo dueño es "otro"
    Y que tiene reservas
    Y sin cabecera "Authorization"
    Cuando se envía una petición GET a "/business/{uuid}/bookings"
    Entonces la respuesta tiene el estado 401
    Y el details debe contener "Full authentication is required to access this resource"
    Y el error debe indicar "Unauthorized"

  @Business @Lectura @Bookings @Consulta @Positivo
  Escenario: consulta las reservas de una empresa sin reservas
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa activa cuyo dueño es "yo"
    Cuando se envía una petición GET a "/business/{uuid}/bookings"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe ser una lista vacía

  @Business @Lectura @Bookings @Consulta @Positivo
  Escenario: consulta las reservas de una empresa paginadas
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa activa cuyo dueño es "yo"
    Y que tiene reservas
    Y el parámetro "size" con valor "2"
    Cuando se envía una petición GET a "/business/{uuid}/bookings"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe contener 2 elementos

  @Business @Lectura @Bookings @Consulta @Negativo
  Escenario: consulta las reservas de una empresa de otro dueño
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa activa cuyo dueño es "otro"
    Y que tiene reservas
    Cuando se envía una petición GET a "/business/{uuid}/bookings"
    Entonces la respuesta tiene el estado 403
    Y el details debe contener "You are not the owner of this business"
    Y el error debe indicar "Forbidden"

  @Business @Lectura @Bookings @Consulta @Negativo
  Escenario: consulta las reservas de una empresa que no existe
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa inexistente con UUID "00000000-0000-0000-0000-000000000000"
    Cuando se envía una petición GET a "/business/{uuid}/bookings"
    Entonces la respuesta tiene el estado 404
    Y el details debe contener "Business not found"
    Y el error debe indicar "Resource not found"

  @Business @Lectura @Bookings @Consulta @Validacion @Negativo
  Escenario: consulta las reservas de una empresa con UUID inválido
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa inexistente con UUID "uuid-no-valido-123"
    Cuando se envía una petición GET a "/business/{uuid}/bookings"
    Entonces la respuesta tiene el estado 400
    Y el details debe contener "Invalid UUID"
    Y el error debe indicar "Validation error"

  @Business @Lectura @Bookings @Consulta @Validacion @Negativo
  Escenario: consulta las reservas de una empresa con un status inválido
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa activa cuyo dueño es "yo"
    Y el parámetro "status" con valor "INVENTADO"
    Cuando se envía una petición GET a "/business/{uuid}/bookings"
    Entonces la respuesta tiene el estado 400
    Y el error debe indicar "Validation error"
