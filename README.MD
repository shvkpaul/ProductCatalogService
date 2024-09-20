# Technologies Used

1. Language : Java
2. Framework : Spring Boot
3. Maven
4. Swagger for documentation

# Build/Execute

Import this maven project in your IDE (I am using IntelliJ IDEA).

# Swagger documentation

Swagger : http://localhost:8080/swagger-ui/index.html

DB CREATION
create database productdb;

create user 'product-team'@'%' identified by 'product-team-pass';

create user 'product-team'@'localhost' identified by 'product-team-pass';

grant all on *.* to 'product-team'@'%' with grant option;

flush privileges;