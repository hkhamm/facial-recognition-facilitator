# Facial Recognition Facilitator

Facilitates communication between the authentication server and the FacePlusPlus API. Receives HTTP POST requests with JSON in the body. Makes HTTP requests to the API to create groups of people. Each person has a set of face images. These are used to determine if a provided image of a person's face matches any of the people in the group.

## Structure

Source code is found in the `app/controllers` directory. The HomeController.java file contains an action that is called when a user communicates to the server at the home address or '/'. This behavior is defined in the routes file at `conf/routes`.


## Dependencies

- [Play Framework with Activator](https://www.playframework.com/)


## Using the app

Run with these commands:

```shell
$ cd Facilitator
$ activator run
```

This starts a web server at localhost, port 9000 (http://localhost:9000).

After it is running users can make HTTP POST requests with properly formatted JSON in the body.


## JSON format

To get the Facilitator server to respond users must post well formatted JSON. The JSON must contain a `internal_id` string to be used as the person_id with the facial recognition service and a `image` string pointing to an image.

### Example:

```json
{
 "internal_id": "b18f599c-e34b-466d-89ee-d9f0d4be8e82",
 "image": "http://www.mywebsite.com/image_0.jpg"
}
```
