# product

Products is an E-commerce and management tool for products
running on Spring boot , React using Microservice architecture

How to install and run instructions:

To run the whole application you need to enter into each folder where the microservices are hosted and run them separetly.

0) in order to run the instances you need postgres database; For more information on how to install posgres and run it on your  server follow the https://hub.docker.com/_/postgres

Go to terminal and run the below commands:

```
docker run --name some-postgres -e POSTGRES_PASSWORD=secset -d -p 5432:5432 postgres
```

Then run:

```
docker exec -it some-postgres psql -U postgres -c "CREATE DATABASE product;"
```


1) Open a new terminal. Go into the registry-microservice directory and run the below command:

```
mvn spring-boot:run 
```

2) Open a new terminal. Go to the ap-gateway-microservice directory and run the below command:

```
mvn spring-boot:run 
```

2) Open a new terminal. Go to the checkout-microservice directory and run the below command:

```
mvn spring-boot:run 
```

3) Open a new terminal. Go to the card-microservice directory and run the below command:

```
mvn spring-boot:run 
```

4) At this step you are running the setup profile of retails microservice. There are some predefined prodcuts that will be imported automatically when running the below command. Open a new terminal. Go to the retails-microservice directory and run the below command:

```
mvn spring-boot:run -Dspring.profiles.active=setup
```

5) And finally to run the Frontend application go to frontend directory and run the below command:

```
npm install
```

Wait a little bit when finished run the below command. This will open a new browser window for you:

```
npm start
```

The application is running on http://localhost:3000

Congratulations! You can now start browsing products and purchasing some of them. If you like you can add new product as from 1500 categories. Then in checkout you can finish your orders and get a confirmation number to track your purchases. 


Overall architecture

![alt text](https://github.com/metao1/product/raw/master/Unbenannte%20Pra%CC%88sentation.jpg)


Screenshot of the product page when it running on http://localhost:3000

![alt text](https://github.com/metao1/product/raw/master/Screenshot%202020-03-31%20at%2022.51.11.png)
