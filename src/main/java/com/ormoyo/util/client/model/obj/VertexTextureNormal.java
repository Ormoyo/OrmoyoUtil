package com.ormoyo.util.client.model.obj;

public class VertexTextureNormal {
	public Vertex vertex;
	public TextureCoords coords;
	public Normal normal;
	
	public VertexTextureNormal(Vertex vertex, TextureCoords coords, Normal normal) {
		this.vertex = vertex;
		this.coords = coords;
		this.normal = normal;
	}
}
