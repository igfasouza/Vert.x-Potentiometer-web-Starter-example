package io.vertx.starter;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Gpio;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;


public class MyFirstVerticle extends AbstractVerticle {


    @Override
    public void start(Future<Void> future) {

        // Create a router object.
        Router router = Router.router(vertx);

        // Bind "/" to our hello message - so we are still compatible.
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                .putHeader("content-type", "text/html")
                .end("<h1>Hello from my first Vert.x 3 application</h1>");
        });

        // Serve static resources from the /assets directory
        router.route("/assets/*").handler(StaticHandler.create("assets"));
        
        //REST End points 
        router.post("/range/:id").handler(this::changePwmValue);
     
        vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(config().getInteger("http.port", 8080), result -> {
            if (result.succeeded()) {
                future.complete();
            } else {
                future.fail(result.cause());
            }
        });
        
    }

    private void changePwmValue(RoutingContext routingContext) {
        String range = routingContext.pathParam("id");
        
        System.out.println(range); //just to see the calls
        
        final GpioController gpio = GpioFactory.getInstance();
        Gpio.pwmSetMode(Gpio.PWM_MODE_MS);
        Gpio.pwmSetRange(100);
        Gpio.pwmSetClock(500);
        
        GpioPinPwmOutput led01 = gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_15, "LeftGreen");
        led01.setPwm(Integer.parseInt(range));

        routingContext.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(200)
                .end(Json.encodePrettily(range));
    }

}