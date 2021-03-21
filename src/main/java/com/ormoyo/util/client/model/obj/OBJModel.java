package com.ormoyo.util.client.model.obj;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.ormoyo.util.OrmoyoUtil;

import net.minecraft.util.ResourceLocation;

public class OBJModel {
	private static final ResourceLocation WHITE = new ResourceLocation(OrmoyoUtil.MODID, "textures/entity/white.png");
    private List<Shape> shapeList = Lists.newArrayList();
    private List<Material> materialList = Lists.newArrayList();
    private int vertexIndex = 1;
    private int textureIndex = 1;
    private int normalIndex = 1;

    public void addShape(Shape shape) {
        this.shapeList.add(shape);
    }
    
    public void addMaterial(Material material) {
    	if(this.materialList.contains(material)) return;
    	Material mat = material;
    	if(material.getColor() == null) mat = new Material(material.getName(), material.getTexture(), 1, 1, 1);
    	if(material.getTexture() == null) mat = new Material(material.getName(), WHITE, material.getColor().x, material.getColor().y, material.getColor().z, material.getColor().w);
    	this.materialList.add(mat);
    }

    public int getVertexIndex() {
        return this.vertexIndex++;
    }

    public int getUVIndex() {
        return this.textureIndex++;
    }
    
    public int getNormalIndex() {
        return this.normalIndex++;
    }
    
    public List<Shape> getShapes() {
    	return Collections.unmodifiableList(this.shapeList);
    }
    
    public List<Material> getMaterials() {
    	return Collections.unmodifiableList(this.materialList);
    }
    
    public Shape getShapeByName(String name) {
    	for(Shape shape : this.shapeList) {
    		if(shape.getName().equals(name)) {
    			return shape;
    		}
    	}
		return null;
    }
    
    public Material getMaterialByName(String name) {
    	for(Material material : this.materialList) {
    		if(material.getName().equals(name)) {
    			return material;
    		}
    	}
		return null;
    }
    
    public void render(float scale) {
    	for(Shape shape : this.shapeList) {
    		shape.render(scale);
    	}
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Shape shape : this.shapeList) {
            for (String l : shape.toString().split(System.lineSeparator())) {
                builder.append(l).append(System.lineSeparator());
            }
        }
        return builder.toString();
    }
}
