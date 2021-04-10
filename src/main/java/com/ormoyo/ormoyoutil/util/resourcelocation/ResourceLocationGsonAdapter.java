package com.ormoyo.ormoyoutil.util.resourcelocation;

import java.io.IOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.minecraft.util.ResourceLocation;

public class ResourceLocationGsonAdapter extends TypeAdapter<ResourceLocation> {

	@Override
	public void write(JsonWriter writer, ResourceLocation location) throws IOException {
		writer.beginObject();
		writer.name("name");
		writer.value(location.toString());
		writer.endObject();
	}

	@Override
	public ResourceLocation read(JsonReader reader) throws IOException {
		reader.beginObject();
		String fieldname = null;
		ResourceLocation location = null;
		while(reader.hasNext()) {
			JsonToken token = reader.peek();
			
			if(token.equals(JsonToken.NAME)) {
				fieldname = reader.nextName();
			}
			
			if(fieldname.equals("name")) {
				token = reader.peek();
				location = new ResourceLocation(reader.nextString());
			}
		}
		reader.endObject();
		return location;
	}

}
