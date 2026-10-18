# language: es
@ui @Negocios
Característica: Catálogo de negocios en la interfaz web
  Como persona autenticada
  Quiero buscar y explorar negocios
  Para encontrar dónde reservar

  Antecedentes:
    Dado que hay una persona con sesión iniciada en la web

  @Positivo
  Escenario: Un negocio creado aparece en el catálogo
    Dado que existe un negocio llamado "Peluquería Lola"
    Cuando la persona abre el catálogo de negocios
    Entonces el catálogo muestra el negocio "Peluquería Lola"

  @Positivo
  Escenario: La búsqueda filtra por nombre
    Dado que existe un negocio llamado "Peluquería Lola"
    Y que existe un negocio llamado "Taller Mecánico Pepe"
    Cuando la persona busca "Lola" en el catálogo
    Entonces el catálogo muestra el negocio "Peluquería Lola"
    Y el catálogo no muestra el negocio "Taller Mecánico Pepe"

  @Negativo
  Escenario: Una búsqueda sin resultados muestra el estado vacío
    Cuando la persona abre el catálogo de negocios
    Y busca "zzzz-no-existe" en el catálogo
    Entonces el catálogo muestra el estado vacío

  @Positivo
  Escenario: La búsqueda se conserva en la URL
    Cuando la persona busca "Lola" en el catálogo
    Entonces la URL contiene el parámetro "q=Lola"
