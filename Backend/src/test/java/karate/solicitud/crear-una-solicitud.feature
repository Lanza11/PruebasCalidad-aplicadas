@ignore
Feature: Crear una solicitud (feature reutilizable)

  Scenario: crear solicitud
    * url baseUrl
    * def body = '{"usuario":"' + usuario + '","tipo":"' + tipo + '","prioridadManual":' + prioridad + ',"fechaCreacion":"' + fecha + '"}'
    Given path '/Solicitud/Crear'
    And request body
    When method POST
    Then status 200