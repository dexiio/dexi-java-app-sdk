Helper library for implementing Dexi Apps
 
Dexi App Client
===============
The DexiAppClient class provides access to API methods on Dexi needed when implementing a external service app.

To create a new client use the DexiClientFactory:
```java
//The account id and API key from your developer account on Dexi
DexiAuth auth = DexiAuth.from(<accountId>, <apiKey>); 
DexiClientFactory clientFactory = new DexiClientFactory(auth);

//Providing a valid activation ID will give you access to information regarding that activation
DexiAppClient = clientFactory.create(<activationId>); 

//There are a few convenience methods for performing typical tasked - with built-in client cache.

//To get the configuration for this particular activation of your app:
MyActivationConfigurationDTO configuration = clientFactory.getActivationConfig(<activationId>, MyActivationConfigurationDTO.class);

//To get configuration provided in a Dexi specific header: 
MyComponentConfigurationDTO configuration = clientFactory.getConfiguration(httpRequest, MyComponentConfigurationDTO.class);
```


Dexi File Pointers
==================
Dexi will never send raw file data as part of it's data payloads. Instead you'll receive what we call a "File pointer".
File pointers have the following format: ```FILE:<mimetype>;<size>;<path>```

To download the contents using a file pointer you can use the **DexiFileClient**: 
```java

if (DexiFileClient.isFileFieldValue(someStringValue)) { //Test a string value to see if its a file pointer
    final DexiFileClient fileClient = dexiClientFactory.create(activationId).files();  

    final DexiFileClient.FileHandle fileHandle = fileClient.getFileFromFieldValue(someStringValue);
    if (fileHandle != null) {
        // File existed
        IOUtils.copyLarge(fileHandler.getStream(), someOutputStream); //Read from InputStream 
    } else {
        //Empty / Non-Existent file 
    }
}
```

HTTP Requests
=============
Whenever dexi sends a request to your service a few headers will always be included to help you identify which account is sending the request. 

All Dexi-specific headers are available as constants in ```DexiAuth.HEADER_*``` and ```DexiPayloadHeaders``` .

```
X-DexiIO-Activation : Activation ID for the request
X-DexiIO-Component  : App Component name for the request
X-DexiIO-Config     : Component configuration serialized to JSON 
```

Below you'll find a list of the different types of requests Dexi sends to your app: All payloads are send as JSON

#### App: Validate Configuration
Called whenever an app activation needs to be validated. Usually while user is creating / updating an activation.

```
Headers: 
X-DexiIO-Activation

Payload: Map<String, Object>
The configuration values of the app

Output: Nothing if succesfull. Send error object to provide proper error feedback to user alongside 400+ HTTP status: 

{ error: true, msg: "The format of field X was invalid" }

```


#### App: Activation Endpoint
Called whenever an app was successfully activated.

```
Headers: 
X-DexiIO-Activation

Payload: Map<String, Object>
The configuration values of the app 

Output: None
```

#### App: Deactivation Endpoint
Called whenever an app was successfully deactivated.

```
Headers: 
X-DexiIO-Activation

Payload: None 

Output: None
```

#### Component: Configuration Validation Endpoint
Called whenever a component configuration needs validation. Usually while creating / updating uses of the apps components.

```
Headers: 
X-DexiIO-Activation
X-DexiIO-Component

Payload: Map<String, Object>
The configuration values of the component

Output: Nothing if succesfull. Send error object to provide proper error feedback to user alongside 400+ HTTP status: 

{ error: true, msg: "The format of field X was invalid" }

```

#### Component: Dynamic Input Schema Endpoint
Called whenever the system needs to resolve the input schema for a component. 

```
Headers: 
X-DexiIO-Activation
X-DexiIO-Component


Payload: Map<String, Object>
The configuration values of the component

Output: Schema (See class) 

```


#### Component: Dynamic Output Schema Endpoint
Called whenever the system needs to resolve the output schema for a component. 

```
Headers: 
X-DexiIO-Activation
X-DexiIO-Component

Payload: Map<String, Object>
The configuration values of the component

Output: Schema (See class) 

```


#### Component: Dynamic Configuration Schema Endpoint
Called whenever the system needs to resolve the configuration schema for a component. 

```
Headers: 
X-DexiIO-Activation
X-DexiIO-Component

Payload: Map<String, Object>
The configuration values of the component

Output: Schema (See class) 

```

#### Component: Data Storage Endpoint
Invocation request made specifically for the ```data-storage``` component type

```
Headers: 
X-DexiIO-Activation
X-DexiIO-Component
X-DexiIO-Config

Payload: Rows

Output: Data according the output schema (or nothing if no output schema defined)

```

#### Component: Data Source Endpoint
Invocation request made specifically for the ```data-source``` component type

```
Headers: 
X-DexiIO-Activation
X-DexiIO-Component
X-DexiIO-Config

Payload: Nothing  
The configuration values of the component

Output: Data according the output schema (or nothing if no output schema defined)

```

#### Component: Data Filter Endpoint
Invocation request made specifically for the ```data-filter``` component type

```
Headers: 
X-DexiIO-Activation
X-DexiIO-Component
X-DexiIO-Config

Payload: Rows
The configuration values of the component

Output: Data according the output schema (or nothing if no output schema defined)

```

#### Component: File Storage Endpoint
Invocation request made specifically for the ```file-storage``` component type

```
Headers: 
X-DexiIO-Activation
X-DexiIO-Component
X-DexiIO-Config
Content-Type

Payload: Stream
The raw contents of the file will be streamed directly to the endpoint 

Output: Nothing

```

#### Component: File Source Endpoint
Invocation request made specifically for the ```file-source``` component type

```
Headers: 
X-DexiIO-Activation
X-DexiIO-Component
X-DexiIO-Config

Payload: Nothing  
The configuration values of the component

Output: Stream
Should output the raw file stream
```

### Error handling
All endpoints must return a status code between 400-499 for configuration issues and 500-599 for internal server issues. 
To provide further information for the user as to what went wrong provide an error object in the output as follows:
```
{ "error": true, "msg": "Something bad happened", "code": 400 }
```

