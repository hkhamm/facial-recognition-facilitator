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
