# Facial Recognition Facilitator

The Facilitator is a HTTP web service that facilitates communication with one or more facial recognition services in order to: register users, associate faces from images to users, train the services to recognize a user’s face in an image, and identify previously trained users in images. Each method is a HTTP POST request with Content-Type: “application/json” in the header. All parameters are passed as JSON in the body.

## Structure

Source code is found in the `app/controllers` directory. Each Controller file contains an action that is called when a user communicates to the server at a specific address. This behavior is defined in the routes file at `conf/routes`.


## Dependencies

- [Play Framework with Activator](https://www.playframework.com/)


## Compiling and running the app

```shell
$ cd Facilitator
$ activator run
```

## Using the app

### Register

Creates and trains new users with the received pictures. Send requests to `http://address:9000/register` with 7 to 8 picture objects. In the response, either facilitatorIds or errors may be null depending on the result.

Request

```
{
  "pictures" : [ 
    {
      "pictureId : int,
      "base64" : string
    } 
  ]
}
```

Response

```
{
  "success": boolean,
  "facilitatorIds" : [ 
    {
      "facType" : string,
      "facId" : string
    } 
  ],
  "errors": [
    {
      "errorCode": int,
      "errorMessage": string
    }
  ]
}
```

### Login

Verifies if the given picture is of a previously registered user. Send requests to `http://address:9000/register` with the facilitatorIds array returned from a successful register request. In the response, errors may be null if success is true.

Request

```
{
  "facilitatorIds" : [ 
    {
      "facType" : string,
      "facId" : string
    } 
  ],
  "picture" : string
}
```

Response

```
{
  "success": boolean,
  "errors": [
    {
      "errorCode": int,
      "errorMessage": string
    }
  ]
}
```
