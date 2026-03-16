Feature: Health Check
  Verifica que el servidor esté corriendo correctamente

  Background:
    * url baseUrl

  Scenario: GET /api/health - servidor responde OK
    Given path '/api/health'
    When method GET
    Then status 200
    And match response.status == 'OK'
    And match response.message == 'Server is running'
    And match response == { status: 'OK', message: 'Server is running' }
