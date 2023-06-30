
# single-customer-account-capabilities

This is a placeholder README.md for a new repository


### **Requirements**

* To run the service locally you need to start the SCA profile from service manager:
* `sm2 --start SCA_FUTURES_ALL`
* 
* Then stop the SCA frontend using the service manager and start is locally:
* `sm2 --stop SINGLE_CUSTOMER_ACCOUNT_CAPABILITIES`
* `sbt run`

### **Using the application**

* Using a tool such as Postman, access the stub data using one of the following routes:
* http://localhost:8423/single-customer-account-capabilities/activities/{NINO}
* http://localhost:8423/single-customer-account-capabilities/actions/{NINO}
* Replace {NINO} with one of the Ninos provided below :

- AA999999A (Only for Action component)
- GG012345C (Activities and Action component)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").