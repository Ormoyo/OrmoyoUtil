package com.ormoyo.util.resourcelocation;

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
		writer.name("domain");
		writer.value(location.getResourceDomain());
		writer.name("path");
		writer.value(location.getResourcePath());
		writer.endObject();
	}

	@Override
	public ResourceLocation read(JsonReader reader) throws IOException {
		reader.beginObject();
		String fieldname = null;
		String domain = null;
		String path = null;
		
		while(reader.hasNext()) {
			JsonToken token = reader.peek();
			
			if(token.equals(JsonToken.NAME)) {
				fieldname = reader.nextName();
			}
			
			if(fieldname.equals("domain")) {
				token = reader.peek();
				domain = reader.nextString();
			}
			
			if(fieldname.equals("path")) {
				token = reader.peek();
				path = reader.nextString();
			}
		}
		reader.endObject();
		return new ResourceLocation(domain, path);
	}

}
