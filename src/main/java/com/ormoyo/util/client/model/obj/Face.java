package com.ormoyo.util.client.model.obj;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public class Face {
    private Shape parentShape;
    private Material material;
    private PolygonType type;
    private List<Vertex> vertexList = Lists.newArrayList();
    private List<TextureCoords> textureCoordsList = Lists.newArrayList();
    private List<Normal> normalList = Lists.newArrayList();

    public Face(Shape shape) {
        this.parentShape = shape;
    }

    public void append(Vertex vertex, TextureCoords textureCoords, Normal normal) {
        this.vertexList.add(vertex);
        this.parentShape.addVertex(vertex);
        this.textureCoordsList.add(textureCoords);
        this.parentShape.addTexCoords(textureCoords);
        this.normalList.add(normal);
        this.parentShape.addNormal(normal);
    }
    
    public Face appendWithoutParent(Vertex vertex, TextureCoords textureCoords, Normal normal) {
        this.vertexList.add(vertex);
        this.textureCoordsList.add(textureCoords);
        this.normalList.add(normal);
        return this;
    }
    
    public List<Vertex> getVertices() {
		return Collections.unmodifiableList(this.vertexList);
    }
    
    public List<TextureCoords> getTextureCoords() {
		return Collections.unmodifiableList(this.textureCoordsList);
    }
    
    public List<Normal> getNormals() {
		return Collections.unmodifiableList(this.normalList);
    }
    
    public PolygonType getType() {
    	return this.type;
    }
    
    public Material getMaterial() {
    	return this.material;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("f");
        for (int i = 0; i < this.vertexList.size(); i++) {
            sb.append(" ").append(this.vertexList.get(i).getIndex()).append("/").append(this.textureCoordsList.get(i).getIndex()).append("/").append(this.normalList.get(i).getIndex());
        }
        return sb.toString();
    }
}
