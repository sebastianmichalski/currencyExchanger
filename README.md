# currencyExchanger


> ### PREREQUISITE
> 
> Java 17 JDK installed, environment variables are set up: 
> https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
> 
> (I suggest using SDKMAN no matter which OS You use: https://sdkman.io/)

## Goal of the task

An APIs to calculate the exchange rates need to be exposed defined here under:
* GET /exchange?from=EUR&to=PLN&date=2020-12-05
  * from and to parameters should accept currency codes
  * date should accept ISO8601 date format (date only)
  * 2020-12-05, EUR and PLN are used as examples
  * Should return the exchange rate between the provided currencies
  * Should use the exchange rate of the day provided in the query parameter date, if
not found return 404 not found
  * date is optional, if not present latest exchange rate information should be used
  * Result should be in JSON format as shown in *Appendix 1*
  * All the information should be retrieved from the cached data in the database
  * A counter in the database should be incremented for both currencies indicating a
request was made for the date selected

To be able to retrieve the data, a scheduler also needs to be implemented that
runs at 12:05 AM GMT and inserts the exchange rates into the database from http://data.fixer.io/api/latest

For this project use as base currency whatever https://fixer.io/ selects for your API key.
    
## running with CLI [dev profile, H2 in memory database]
```./mvnw spring-boot:run -Dspring-boot.run.profiles=dev```

## running tests with CLI
```./mvnw test```

## Swagger UI access when application is running
[Swagger UI](http://localhost:8080/swagger-ui/index.html)

## App properties
datasource, external API url, api-key and spreads are customizable by editing
* [application-dev.yml](src/main/resources/application-dev.yml) file [DEV]
* [application-prod.yml](src/main/resources/application-prod.yml) file [PROD]

## Technologies
* Java
* Spring (Boot, AOP, Retry)
* maven
* Hibernate
* JPA
* Swagger
* Slf4J
* lombok
* jUnit5

---
### Ideas for future improvements 
1. Adapt to reactive world with Spring Webflux
2. Add k8s, helm and terraform configuration
3. Write some performance and e2e tests
---

### Appendix 1

```
{
  “from” : “EUR”,
  “to” : “PLN”,
  “exchange”: 4.4405487565413254124
}
```