# language: es
@ui @wip @MisNegocios
Característica: Gestión de negocios propios en la interfaz web
  Como persona que quiere recibir reservas
  Quiero publicar y eliminar mis negocios
  Para que otras personas puedan reservar en ellos

  Antecedentes:
    Dado que hay una persona con sesión iniciada en la web

  @Positivo
  Escenario: Publicar un negocio nuevo
    Cuando la persona publica el negocio "Cafetería Sol" con CIF "B12345678" y dirección "Calle Mayor 1"
    Entonces "Mis negocios" muestra el negocio "Cafetería Sol"

  @Validacion
  Escenario: Un CIF con formato inválido se rechaza
    Cuando la persona publica el negocio "Cafetería Sol" con CIF "123" y dirección "Calle Mayor 1"
    Entonces el formulario de negocio muestra un error en "CIF"

  @Positivo
  Escenario: Eliminar un negocio pide confirmación
    Dado que la persona ya tiene publicado el negocio "Cafetería Sol"
    Cuando la persona elimina el negocio "Cafetería Sol" y confirma en el modal
    Entonces "Mis negocios" no muestra el negocio "Cafetería Sol"

  @Positivo
  Escenario: Publicar el primer negocio cambia el menú de navegación
    Cuando la persona publica el negocio "Cafetería Sol" con CIF "B12345678" y dirección "Calle Mayor 1"
    Entonces el menú principal muestra el enlace "Mis negocios"
