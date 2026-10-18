# language: es
@wip @Booking
Característica: gestión de reservas /booking
  Como cliente, dueño de empresa o administrador de la API de reservas
  quiero crear, consultar y gestionar reservas
  para organizar las citas con las empresas de la plataforma

  Antecedentes:
    Dado que el servicio de reservas está disponible

  @Booking @Lectura @Consulta @Positivo
  Esquema del escenario: consulta de una reserva por quien tiene acceso
    Dado una reserva en estado "PENDING" del cliente
    Y una sesión iniciada como "<actor>"
    Cuando se lanza una petición GET a "/booking/{uuid}"
    Entonces el código de respuesta es 200
    Y la respuesta debe contener la reserva solicitada

    Ejemplos:
      | actor   |
      | cliente |
      | dueño   |
      | admin   |

  @Booking @Lectura @Consulta @Negativo
  Esquema del escenario: consulta de una reserva ajena
    Dado una reserva en estado "PENDING" del cliente
    Y una sesión iniciada como "<actor>"
    Cuando se lanza una petición GET a "/booking/{uuid}"
    Entonces el código de respuesta es 403
    Y el detalle del error contiene "You cannot access this booking"
    Y el tipo de error es "Forbidden"

    Ejemplos:
      | actor        |
      | otro cliente |
      | otro dueño   |

  @Booking @Lectura @Consulta @Negativo
  Escenario: consulta de una reserva que no existe
    Dado una sesión iniciada como "cliente"
    Y una reserva inexistente con UUID "00000000-0000-0000-0000-000000000000"
    Cuando se lanza una petición GET a "/booking/{uuid}"
    Entonces el código de respuesta es 404
    Y el detalle del error contiene "Booking not found"
    Y el tipo de error es "Resource not found"

  @Booking @Lectura @Consulta @Validacion @Negativo
  Escenario: consulta de una reserva con UUID inválido
    Dado una sesión iniciada como "cliente"
    Y una reserva inexistente con UUID "uuid-no-valido-123"
    Cuando se lanza una petición GET a "/booking/{uuid}"
    Entonces el código de respuesta es 400
    Y el detalle del error contiene "Invalid UUID"
    Y el tipo de error es "Validation error"

  @Booking @Lectura @Consulta @Seguridad @Negativo
  Escenario: consulta de una reserva sin token de autorización
    Dado una reserva en estado "PENDING" del cliente
    Y sin sesión iniciada
    Cuando se lanza una petición GET a "/booking/{uuid}"
    Entonces el código de respuesta es 401
    Y el tipo de error es "Unauthorized"

  @Booking @Lectura @Busqueda @Positivo
  Escenario: consulta de mis reservas devuelve solo las del cliente
    Dado el cliente tiene reservas en varios estados
    Y una sesión iniciada como "cliente"
    Cuando se lanza una petición GET a "/booking/mine"
    Entonces el código de respuesta es 200
    Y la respuesta debe contener solo reservas del cliente

  @Booking @Lectura @Busqueda @Positivo
  Esquema del escenario: consulta de mis reservas filtradas por estado
    Dado el cliente tiene reservas en varios estados
    Y una sesión iniciada como "cliente"
    Y el filtro "status" con valor "<estado>"
    Cuando se lanza una petición GET a "/booking/mine"
    Entonces el código de respuesta es 200
    Y la respuesta debe contener las reservas del cliente en estado "<estado>"

    Ejemplos:
      | estado    |
      | PENDING   |
      | CONFIRMED |
      | CANCELLED |
      | COMPLETED |

  @Booking @Lectura @Busqueda @Positivo
  Escenario: consulta de mis reservas paginadas
    Dado el cliente tiene reservas en varios estados
    Y una sesión iniciada como "cliente"
    Y el filtro "size" con valor "2"
    Cuando se lanza una petición GET a "/booking/mine"
    Entonces el código de respuesta es 200
    Y la respuesta contiene 2 reservas

  @Booking @Lectura @Busqueda @Positivo
  Escenario: consulta de mis reservas sin reservas
    Dado el cliente tiene reservas en varios estados
    Y una sesión iniciada como "otro dueño"
    Cuando se lanza una petición GET a "/booking/mine"
    Entonces el código de respuesta es 200
    Y la respuesta contiene 0 reservas

  @Booking @Lectura @Busqueda @Validacion @Negativo
  Escenario: consulta de mis reservas con un estado inválido
    Dado una sesión iniciada como "cliente"
    Y el filtro "status" con valor "INVENTADO"
    Cuando se lanza una petición GET a "/booking/mine"
    Entonces el código de respuesta es 400
    Y el tipo de error es "Validation error"

  @Booking @Lectura @Busqueda @Seguridad @Negativo
  Escenario: consulta de mis reservas sin token de autorización
    Dado sin sesión iniciada
    Cuando se lanza una petición GET a "/booking/mine"
    Entonces el código de respuesta es 401
    Y el tipo de error es "Unauthorized"

  @Booking @Guardado @Positivo
  Esquema del escenario: crea una reserva en una empresa ajena
    Dado una sesión iniciada como "<actor>"
    Y los datos de una reserva válida
    Cuando se lanza una petición POST a "/booking"
    Entonces el código de respuesta es 200
    Y la respuesta debe contener la reserva creada en estado "PENDING"

    Ejemplos:
      | actor      |
      | cliente    |
      | otro dueño |
      | admin      |

  @Booking @Guardado @Positivo
  Escenario: el estado enviado al crear una reserva se ignora
    Dado una sesión iniciada como "cliente"
    Y los datos de una reserva válida con "status" igual a "CONFIRMED"
    Cuando se lanza una petición POST a "/booking"
    Entonces el código de respuesta es 200
    Y la respuesta debe contener la reserva creada en estado "PENDING"

  @Booking @Guardado @Negativo
  Escenario: el dueño no puede reservar en su propia empresa
    Dado una sesión iniciada como "dueño"
    Y los datos de una reserva válida
    Cuando se lanza una petición POST a "/booking"
    Entonces el código de respuesta es 403
    Y el detalle del error contiene "Owners cannot book their own business"
    Y el tipo de error es "Forbidden"

  @Booking @Guardado @Negativo
  Escenario: crea una reserva en una empresa que no existe
    Dado una sesión iniciada como "cliente"
    Y los datos de una reserva para una empresa inexistente con UUID "00000000-0000-0000-0000-000000000000"
    Cuando se lanza una petición POST a "/booking"
    Entonces el código de respuesta es 404
    Y el detalle del error contiene "Business not found"
    Y el tipo de error es "Resource not found"

  @Booking @Guardado @Negativo
  Escenario: crea una reserva en una empresa inactiva
    Dado una sesión iniciada como "cliente"
    Y los datos de una reserva para la empresa inactiva
    Cuando se lanza una petición POST a "/booking"
    Entonces el código de respuesta es 404
    Y el detalle del error contiene "Business not found"
    Y el tipo de error es "Resource not found"

  @Booking @Guardado @Validacion @Negativo
  Esquema del escenario: crea una reserva con un campo inválido
    Dado una sesión iniciada como "cliente"
    Y los datos de una reserva válida con "<campo>" igual a "<valor>"
    Cuando se lanza una petición POST a "/booking"
    Entonces el código de respuesta es 400
    Y el detalle del error contiene "<mensaje>"
    Y el tipo de error es "Validation error"

    Ejemplos:
      | campo       | valor               | mensaje                                         |
      | businessId  |                     | businessId: Business is required                |
      | bookingTime |                     | bookingTime: Booking time is required           |
      | bookingTime | 2020-01-01T10:00:00 | bookingTime: Booking time must be in the future |
      | partySize   |                     | partySize: Party size is required               |
      | partySize   | 0                   | partySize: There must be at least 1 person      |
      | price       | -1                  | price: Price must not be negative               |

  @Booking @Guardado @Validacion @Negativo
  Escenario: crea una reserva con una nota demasiado larga
    Dado una sesión iniciada como "cliente"
    Y los datos de una reserva con una nota de 501 caracteres
    Cuando se lanza una petición POST a "/booking"
    Entonces el código de respuesta es 400
    Y el detalle del error contiene "note: Note must be at most 500 characters"
    Y el tipo de error es "Validation error"

  @Booking @Guardado @Validacion @Negativo
  Escenario: crea una reserva con un cuerpo vacío
    Dado una sesión iniciada como "cliente"
    Y los datos de una reserva vacíos
    Cuando se lanza una petición POST a "/booking"
    Entonces el código de respuesta es 400
    Y el detalle del error contiene "businessId: Business is required"
    Y el detalle del error contiene "bookingTime: Booking time is required"
    Y el detalle del error contiene "partySize: Party size is required"
    Y el tipo de error es "Validation error"

  @Booking @Guardado @Validacion @Negativo
  Escenario: crea una reserva sin cuerpo
    Dado una sesión iniciada como "cliente"
    Cuando se lanza una petición POST a "/booking"
    Entonces el código de respuesta es 400
    Y el detalle del error contiene "Malformed or unreadable request body"
    Y el tipo de error es "Validation error"

  @Booking @Guardado @Seguridad @Negativo
  Escenario: crea una reserva sin token de autorización
    Dado los datos de una reserva válida
    Y sin sesión iniciada
    Cuando se lanza una petición POST a "/booking"
    Entonces el código de respuesta es 401
    Y el tipo de error es "Unauthorized"

  @Booking @Actualizar @Positivo
  Escenario: el titular modifica su reserva pendiente
    Dado una reserva en estado "PENDING" del cliente
    Y una sesión iniciada como "cliente"
    Y los datos para modificar la reserva
    Cuando se lanza una petición PUT a "/booking/{uuid}"
    Entonces el código de respuesta es 200
    Y la respuesta debe contener la reserva modificada

  @Booking @Actualizar @Negativo
  Esquema del escenario: solo el titular puede modificar la reserva
    Dado una reserva en estado "PENDING" del cliente
    Y una sesión iniciada como "<actor>"
    Y los datos para modificar la reserva
    Cuando se lanza una petición PUT a "/booking/{uuid}"
    Entonces el código de respuesta es 403
    Y el detalle del error contiene "Only the holder can modify this booking"
    Y el tipo de error es "Forbidden"

    Ejemplos:
      | actor        |
      | otro cliente |
      | dueño        |
      | admin        |

  @Booking @Actualizar @Negativo
  Esquema del escenario: no se puede modificar una reserva que no está pendiente
    Dado una reserva en estado "<estado>" del cliente
    Y una sesión iniciada como "cliente"
    Y los datos para modificar la reserva
    Cuando se lanza una petición PUT a "/booking/{uuid}"
    Entonces el código de respuesta es 409
    Y el detalle del error contiene "Only pending bookings can be modified"
    Y el tipo de error es "Conflict"

    Ejemplos:
      | estado    |
      | CONFIRMED |
      | CANCELLED |
      | COMPLETED |

  @Booking @Actualizar @Validacion @Negativo
  Esquema del escenario: modifica una reserva con un campo inválido
    Dado una reserva en estado "PENDING" del cliente
    Y una sesión iniciada como "cliente"
    Y los datos para modificar la reserva con "<campo>" igual a "<valor>"
    Cuando se lanza una petición PUT a "/booking/{uuid}"
    Entonces el código de respuesta es 400
    Y el detalle del error contiene "<mensaje>"
    Y el tipo de error es "Validation error"

    Ejemplos:
      | campo       | valor               | mensaje                                         |
      | bookingTime | 2020-01-01T10:00:00 | bookingTime: Booking time must be in the future |
      | partySize   | 0                   | partySize: There must be at least 1 person      |
      | partySize   |                     | partySize: Party size is required               |

  @Booking @Actualizar @Negativo
  Escenario: modifica una reserva que no existe
    Dado una sesión iniciada como "cliente"
    Y una reserva inexistente con UUID "00000000-0000-0000-0000-000000000000"
    Y los datos para modificar la reserva
    Cuando se lanza una petición PUT a "/booking/{uuid}"
    Entonces el código de respuesta es 404
    Y el detalle del error contiene "Booking not found"
    Y el tipo de error es "Resource not found"

  @Booking @Actualizar @Seguridad @Negativo
  Escenario: modifica una reserva sin token de autorización
    Dado una reserva en estado "PENDING" del cliente
    Y los datos para modificar la reserva
    Y sin sesión iniciada
    Cuando se lanza una petición PUT a "/booking/{uuid}"
    Entonces el código de respuesta es 401
    Y el tipo de error es "Unauthorized"

  @Booking @Estado @Positivo
  Esquema del escenario: cambios de estado permitidos
    Dado una reserva en estado "<inicial>" del cliente
    Y una sesión iniciada como "<actor>"
    Cuando se lanza una petición PATCH a "/booking/{uuid}/<accion>"
    Entonces el código de respuesta es 200
    Y la reserva debe quedar en estado "<final>"

    Ejemplos:
      | actor   | inicial   | accion   | final     |
      | dueño   | PENDING   | confirm  | CONFIRMED |
      | admin   | PENDING   | confirm  | CONFIRMED |
      | dueño   | CONFIRMED | complete | COMPLETED |
      | admin   | CONFIRMED | complete | COMPLETED |
      | cliente | PENDING   | cancel   | CANCELLED |
      | cliente | CONFIRMED | cancel   | CANCELLED |
      | dueño   | PENDING   | cancel   | CANCELLED |
      | admin   | CONFIRMED | cancel   | CANCELLED |

  @Booking @Estado @Negativo
  Esquema del escenario: cambios de estado no permitidos
    Dado una reserva en estado "<inicial>" del cliente
    Y una sesión iniciada como "<actor>"
    Cuando se lanza una petición PATCH a "/booking/{uuid}/<accion>"
    Entonces el código de respuesta es 409
    Y el detalle del error contiene "Cannot change a booking from <inicial> to <destino>"
    Y el tipo de error es "Conflict"
    Y la reserva debe quedar en estado "<inicial>"

    Ejemplos:
      | actor   | inicial   | accion   | destino   |
      | dueño   | PENDING   | complete | COMPLETED |
      | dueño   | CONFIRMED | confirm  | CONFIRMED |
      | dueño   | CANCELLED | confirm  | CONFIRMED |
      | dueño   | COMPLETED | cancel   | CANCELLED |
      | cliente | CANCELLED | cancel   | CANCELLED |

  @Booking @Estado @Negativo
  Esquema del escenario: el cliente no puede confirmar ni completar reservas
    Dado una reserva en estado "<inicial>" del cliente
    Y una sesión iniciada como "cliente"
    Cuando se lanza una petición PATCH a "/booking/{uuid}/<accion>"
    Entonces el código de respuesta es 403
    Y el detalle del error contiene "You do not have permission to perform this action"
    Y el tipo de error es "Forbidden"
    Y la reserva debe quedar en estado "<inicial>"

    Ejemplos:
      | inicial   | accion   |
      | PENDING   | confirm  |
      | CONFIRMED | complete |

  @Booking @Estado @Negativo
  Esquema del escenario: otro dueño no puede gestionar reservas de una empresa ajena
    Dado una reserva en estado "<inicial>" del cliente
    Y una sesión iniciada como "otro dueño"
    Cuando se lanza una petición PATCH a "/booking/{uuid}/<accion>"
    Entonces el código de respuesta es 403
    Y el detalle del error contiene "Only the business owner can manage this booking"
    Y el tipo de error es "Forbidden"
    Y la reserva debe quedar en estado "<inicial>"

    Ejemplos:
      | inicial   | accion   |
      | PENDING   | confirm  |
      | CONFIRMED | complete |

  @Booking @Estado @Negativo
  Esquema del escenario: cancelar una reserva ajena
    Dado una reserva en estado "PENDING" del cliente
    Y una sesión iniciada como "<actor>"
    Cuando se lanza una petición PATCH a "/booking/{uuid}/cancel"
    Entonces el código de respuesta es 403
    Y el detalle del error contiene "You cannot access this booking"
    Y el tipo de error es "Forbidden"
    Y la reserva debe quedar en estado "PENDING"

    Ejemplos:
      | actor        |
      | otro cliente |
      | otro dueño   |

  @Booking @Estado @Negativo
  Esquema del escenario: cambiar el estado de una reserva que no existe
    Dado una sesión iniciada como "admin"
    Y una reserva inexistente con UUID "00000000-0000-0000-0000-000000000000"
    Cuando se lanza una petición PATCH a "/booking/{uuid}/<accion>"
    Entonces el código de respuesta es 404
    Y el detalle del error contiene "Booking not found"
    Y el tipo de error es "Resource not found"

    Ejemplos:
      | accion   |
      | confirm  |
      | complete |
      | cancel   |

  @Booking @Estado @Seguridad @Negativo
  Esquema del escenario: cambiar el estado de una reserva sin token de autorización
    Dado una reserva en estado "PENDING" del cliente
    Y sin sesión iniciada
    Cuando se lanza una petición PATCH a "/booking/{uuid}/<accion>"
    Entonces el código de respuesta es 401
    Y el tipo de error es "Unauthorized"
    Y la reserva debe quedar en estado "PENDING"

    Ejemplos:
      | accion   |
      | confirm  |
      | complete |
      | cancel   |
