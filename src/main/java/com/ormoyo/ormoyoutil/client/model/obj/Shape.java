package com.ormoyo.ormoyoutil.client.model.obj;

import java.util.Collections;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import com.google.common.collect.Lists;
import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.client.RenderHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

public class Shape implements Cloneable {
    private String name;
    private OBJModel model;
    private Shape parent;
    private List<Face> faceList = Lists.newArrayList();
    private List<Vertex> vertexList = Lists.newArrayList();
    private List<TextureCoords> textureCoordsList = Lists.newArrayList();
    private List<Normal> normalList = Lists.newArrayList();
    private List<Shape> childList = Lists.newArrayList();
    

    public Shape(OBJModel model, String name) {
        this.model = model;
        this.name = name;
    }
    
    public Shape(OBJModel model, Shape parent, String name) {
        this.model = model;
        this.parent = parent;
        this.name = name;
    }

    public Matrix3f rotationMatrix(float angle, float x, float y, float z) {
        angle *= (float) Math.PI / 180.0F;
        Vector3f axis = new Vector3f(x, y, z);
        axis.normalise();
        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);
        float oc = 1.0f - c;

        Matrix3f mat = new Matrix3f();
        mat.m00 = oc * axis.x * axis.x + c;
        mat.m01 = oc * axis.x * axis.y - axis.z * s;
        mat.m02 = oc * axis.z * axis.x + axis.y * s;
        mat.m10 = oc * axis.x * axis.y + axis.z * s;
        mat.m11 = oc * axis.y * axis.y + c;
        mat.m12 = oc * axis.y * axis.z - axis.x * s;
        mat.m20 = oc * axis.z * axis.x - axis.y * s;
        mat.m21 = oc * axis.y * axis.z + axis.x * s;
        mat.m22 = oc * axis.z * axis.z + c;
        return mat;
    }

    public void addFace(Face face) {
        this.faceList.add(face);
    }

    public void addVertex(Vertex vertex) {
        this.vertexList.add(vertex);
        vertex.register(this.model);
    }

    public void addTexCoords(TextureCoords textureCoords) {
        this.textureCoordsList.add(textureCoords);
        textureCoords.register(this.model);
    }
    
    public void addNormal(Normal normal) {
        this.normalList.add(normal);
        normal.register(this.model);
    }
    
    public void addChildShape(Shape shape) {
    	if(shape == this.parent) {
    		OrmoyoUtil.LOGGER.error("Cannot make parent shape to child shape");
    		return;
    	}
    	if(this == shape) {
    		OrmoyoUtil.LOGGER.error("Cannot make shape it's own child");
    		return;
    	}
    	if(this.childList.contains(shape)) {
    		OrmoyoUtil.LOGGER.error("Shape already contains child shape");
    		return;
    	}
    	this.childList.add(shape);
    	shape.parent = this;
    }

    public void translate(Vector3f translationVector) {
        for (Vertex vertex : this.vertexList) {
            Vector3f.add(vertex.getPosition(), translationVector, vertex.getPosition());
        }
    }

    public void scale(Vector3f scaleVector) {
        for (Vertex vertex : this.vertexList) {
            vertex.getPosition().x *= scaleVector.x;
            vertex.getPosition().y *= scaleVector.y;
            vertex.getPosition().z *= scaleVector.z;
        }
    }

    public void rotate(float angle, float x, float y, float z) {
        Matrix3f rotationMatrix = this.rotationMatrix(angle, x, y, z);
        for (Vertex vertex : this.vertexList) {
            Matrix3f.transform(rotationMatrix, vertex.getPosition(), vertex.getPosition());
        }
        for(Normal normal : this.normalList) {
        	Matrix3f.transform(rotationMatrix, normal.getVector(), normal.getVector());
        }
    }
    
    public void render(float scale) {
    	ResourceLocation prevTexture = null;
    	Tessellator tess = Tessellator.getInstance();
    	BufferBuilder bb = tess.getBuffer();
    	RenderHelper.setupOpacity();
    	GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
    	for(Face face : this.faceList) {
    		if(face.getMaterial().getTexture() != null && !face.getMaterial().getTexture().equals(prevTexture)) {
    			Minecraft.getMinecraft().getTextureManager().bindTexture(face.getMaterial().getTexture());
    			prevTexture = face.getMaterial().getTexture();
    		}
    		bb.begin(face.getType().mode, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
    		for(int i = 0; i < face.getVertices().size(); i++) {
    			Vertex vertex = face.getVertices().get(i);
    			TextureCoords coords = face.getTextureCoords().get(i);
    			Normal normal = face.getNormals().get(i);
    			bb.pos(vertex.getPosition().x * scale, vertex.getPosition().y * scale, vertex.getPosition().z * scale).tex(coords.getCoords().x, coords.getCoords().y).color(face.getMaterial().getColor().x, face.getMaterial().getColor().y, face.getMaterial().getColor().z, face.getMaterial().getColor().w).normal(normal.getVector().x, normal.getVector().y, normal.getVector().z).endVertex();
    		}
    		tess.draw();
    	}
    	GlStateManager.disableBlend();
    	GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
    }
    
    public String getName() {
    	return this.name;
    }
    
    public List<Face> getFaces() {
		return Collections.unmodifiableList(this.faceList);
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
    
    public Shape getParent() {
    	return this.parent;
    }
    
    public OBJModel getModel() {
    	return this.model;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("o ").append(this.name).append(System.lineSeparator());
        for (Vertex vertex : this.vertexList) {
            builder.append(vertex.toString()).append(System.lineSeparator());
        }
        for (TextureCoords textureCoords : this.textureCoordsList) {
            builder.append(textureCoords.toString()).append(System.lineSeparator());
        }
        for(Normal normal : this.normalList) {
        	builder.append(normal.toString()).append(System.lineSeparator());
        }
        for (Face face : this.faceList) {
            builder.append(face.toString()).append(System.lineSeparator());
        }
        return builder.toString();
    }
    
    public Shape clone(OBJModel model) {
    	Shape shape = null;
    	try {
			shape = (Shape) this.clone();
			shape.model = model;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return shape;
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
    	Shape shape = (Shape) super.clone();
    	String name = shape.name;
    	shape.name = new String(name);
    	return shape;
    }
}
