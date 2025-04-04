package com.pfe.flight.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.gson.JsonObject;
import java.io.IOException;

public class GsonJsonObjectSerializer extends StdSerializer<JsonObject> {

    public GsonJsonObjectSerializer() {
        super(JsonObject.class);
    }

    @Override
    public void serialize(JsonObject value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeRawValue(value.toString());
    }
}
