package com.az.gitember.data;

import com.az.gitember.service.CipherService;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class MaskStringValueSerializer extends StdSerializer<String>  {

    public MaskStringValueSerializer() {
        super(String.class);
    }

    public MaskStringValueSerializer(Class<String> t) {
        super(String.class);
    }

    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        jsonGenerator.writeString(CipherService.crypt(value));
    }
}
