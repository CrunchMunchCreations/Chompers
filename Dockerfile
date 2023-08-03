FROM gcr.io/distroless/java17-debian11

ENV APP_HOME=/app

COPY ./Sprinkles-1.0.0-all.jar $APP_HOME/

WORKDIR $APP_HOME

CMD ["Sprinkles-1.0.0-all.jar"]