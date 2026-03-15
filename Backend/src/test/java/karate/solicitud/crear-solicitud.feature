Feature: Crear Solicitud - POST /Solicitud/Crear
  Pruebas del endpoint de creación de solicitudes

  Background:
    * url baseUrl
    * path '/Solicitud/Crear'

  # =============================================
  # CASOS EXITOSOS
  # =============================================

  Scenario: Crear solicitud de tipo INCIDENTE con todos los campos válidos
    Given request
      """
      {
        "usuario": "Juan Perez",
        "tipo": "INCIDENTE",
        "prioridadManual": 1,
        "fechaCreacion": "2024-01-15T10:00:00"
      }
      """
    When method POST
    Then status 200
    And match response.id == '#number'
    And match response.usuario == 'Juan Perez'
    And match response.tipo == 'INCIDENTE'
    And match response.prioridadManual == 1
    And match response.fechaCreacion == '#string'

  Scenario: Crear solicitud de tipo REQUERIMIENTO con prioridad máxima
    Given request
      """
      {
        "usuario": "Maria Garcia",
        "tipo": "REQUERIMIENTO",
        "prioridadManual": 5,
        "fechaCreacion": "2024-03-01T08:30:00"
      }
      """
    When method POST
    Then status 200
    And match response.tipo == 'REQUERIMIENTO'
    And match response.prioridadManual == 5
    And match response.usuario == 'Maria Garcia'

  Scenario: Crear solicitud de tipo CONSULTA con prioridad mínima
    Given request
      """
      {
        "usuario": "Carlos Lopez",
        "tipo": "CONSULTA",
        "prioridadManual": 1,
        "fechaCreacion": "2024-06-10T14:00:00"
      }
      """
    When method POST
    Then status 200
    And match response.tipo == 'CONSULTA'
    And match response.prioridadManual == 1

  Scenario: Crear solicitud sin fechaCreacion - debe autoasignar fecha
    Given request
      """
      {
        "usuario": "Ana Torres",
        "tipo": "INCIDENTE",
        "prioridadManual": 3
      }
      """
    When method POST
    Then status 200
    And match response.id == '#number'
    And match response.fechaCreacion == '#string'
    And match response.usuario == 'Ana Torres'

  Scenario: Crear solicitud con nombre de usuario en longitud mínima (2 chars)
    Given request
      """
      {
        "usuario": "AB",
        "tipo": "CONSULTA",
        "prioridadManual": 2,
        "fechaCreacion": "2024-01-01T00:00:00"
      }
      """
    When method POST
    Then status 200
    And match response.usuario == 'AB'

  Scenario: Crear solicitud con nombre de usuario en longitud máxima (100 chars)
    * def nombreLargo = 'A'.repeat ? 'A'.repeat(100) : 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
    Given request
      """
      {
        "usuario": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
        "tipo": "REQUERIMIENTO",
        "prioridadManual": 4,
        "fechaCreacion": "2024-01-01T00:00:00"
      }
      """
    When method POST
    Then status 200

  Scenario: Crear solicitud con prioridad 3 (valor intermedio)
    Given request
      """
      {
        "usuario": "Pedro Ramirez",
        "tipo": "INCIDENTE",
        "prioridadManual": 3,
        "fechaCreacion": "2024-05-20T09:00:00"
      }
      """
    When method POST
    Then status 200
    And match response.prioridadManual == 3

  # =============================================
  # VALIDACIONES - USUARIO
  # =============================================

  Scenario: Error 400 - usuario vacío
    Given request
      """
      {
        "usuario": "",
        "tipo": "INCIDENTE",
        "prioridadManual": 1,
        "fechaCreacion": "2024-01-01T00:00:00"
      }
      """
    When method POST
    Then status 400
    And match response.status == 400
    And match response.error == 'Validation Error'
    And match response.validationErrors.usuario == '#string'

  Scenario: Error 400 - usuario null
    Given request
      """
      {
        "tipo": "INCIDENTE",
        "prioridadManual": 1,
        "fechaCreacion": "2024-01-01T00:00:00"
      }
      """
    When method POST
    Then status 400
    And match response.validationErrors.usuario == '#string'

  Scenario: Error 400 - usuario con menos de 2 caracteres
    Given request
      """
      {
        "usuario": "A",
        "tipo": "INCIDENTE",
        "prioridadManual": 1,
        "fechaCreacion": "2024-01-01T00:00:00"
      }
      """
    When method POST
    Then status 400
    And match response.validationErrors.usuario == '#string'

  # =============================================
  # VALIDACIONES - TIPO
  # =============================================

  Scenario: Error 400 - tipo null
    Given request
      """
      {
        "usuario": "Juan Perez",
        "prioridadManual": 1,
        "fechaCreacion": "2024-01-01T00:00:00"
      }
      """
    When method POST
    Then status 400
    And match response.validationErrors.tipo == '#string'

  Scenario: Error 400 - tipo inválido (valor no permitido)
    Given request
      """
      {
        "usuario": "Juan Perez",
        "tipo": "URGENTE",
        "prioridadManual": 1,
        "fechaCreacion": "2024-01-01T00:00:00"
      }
      """
    When method POST
    Then status 400
    And match response.status == 400

  # =============================================
  # VALIDACIONES - PRIORIDAD MANUAL
  # =============================================

  Scenario: Error 400 - prioridadManual null
    Given request
      """
      {
        "usuario": "Juan Perez",
        "tipo": "INCIDENTE",
        "fechaCreacion": "2024-01-01T00:00:00"
      }
      """
    When method POST
    Then status 400
    And match response.validationErrors.prioridadManual == '#string'

  Scenario: Error 400 - prioridadManual menor a 1 (valor 0)
    Given request
      """
      {
        "usuario": "Juan Perez",
        "tipo": "INCIDENTE",
        "prioridadManual": 0,
        "fechaCreacion": "2024-01-01T00:00:00"
      }
      """
    When method POST
    Then status 400
    And match response.validationErrors.prioridadManual == '#string'

  Scenario: Error 400 - prioridadManual mayor a 5 (valor 6)
    Given request
      """
      {
        "usuario": "Juan Perez",
        "tipo": "INCIDENTE",
        "prioridadManual": 6,
        "fechaCreacion": "2024-01-01T00:00:00"
      }
      """
    When method POST
    Then status 400
    And match response.validationErrors.prioridadManual == '#string'

  Scenario: Error 400 - prioridadManual negativa
    Given request
      """
      {
        "usuario": "Juan Perez",
        "tipo": "INCIDENTE",
        "prioridadManual": -1,
        "fechaCreacion": "2024-01-01T00:00:00"
      }
      """
    When method POST
    Then status 400
    And match response.validationErrors.prioridadManual == '#string'

  # =============================================
  # VALIDACIONES - FECHA
  # =============================================

  Scenario: Error 400 - fechaCreacion en el futuro
    Given request
      """
      {
        "usuario": "Juan Perez",
        "tipo": "INCIDENTE",
        "prioridadManual": 3,
        "fechaCreacion": "2099-12-31T23:59:59"
      }
      """
    When method POST
    Then status 400
    And match response.validationErrors.fechaCreacion == '#string'

  # =============================================
  # VALIDACIONES - FORMATO JSON
  # =============================================

  Scenario: Error 400 - body vacío
    Given request {}
    When method POST
    Then status 400
    And match response.status == 400

  Scenario: Error 400 - JSON malformado (tipo de dato incorrecto)
    Given request
      """
      {
        "usuario": "Juan Perez",
        "tipo": "INCIDENTE",
        "prioridadManual": "alta",
        "fechaCreacion": "2024-01-01T00:00:00"
      }
      """
    When method POST
    Then status 400

  # =============================================
  # MÚLTIPLES ERRORES DE VALIDACIÓN
  # =============================================

  Scenario: Error 400 - múltiples campos inválidos simultáneamente
    Given request
      """
      {
        "usuario": "",
        "prioridadManual": 10
      }
      """
    When method POST
    Then status 400
    And match response.validationErrors == '#object'
    And match response.validationErrors.usuario == '#string'
    And match response.validationErrors.prioridadManual == '#string'
