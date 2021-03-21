package com.ormoyo.util.client.model.obj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.lwjgl.util.vector.Vector3f;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.ormoyo.util.OrmoyoUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class OBJLoader {
	private static Material defaultMaterial = new Material("default", 1, 1, 1);
	
	/**
	 * Loads a file from the resource location
	 * @param file The {@link ResourceLocation} to point to the obj file
	 * @apiNote Don't need to type extension
	 */
	public static OBJModel loadOBJModel(ResourceLocation file) {
		return loadOBJModel(file, true);
	}
	
	/**
	 * Loads a file from the resource location
	 * @param file The {@link ResourceLocation} to point to the obj file
	 * @param removeDuplicateVertices If true every duplicate vertex will not get loaded
	 * @apiNote Don't need to type extension
	 */
	public static OBJModel loadOBJModel(ResourceLocation file, boolean removeDuplicateVertices) {
		try {
			OBJModel model = new OBJModel();
			System.out.println("BRO");
			InputStream stream;
			if(file.getResourcePath().endsWith(".obj")) {
				stream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(file.getResourceDomain(), file.getResourcePath())).getInputStream();
			}else {
				stream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(file.getResourceDomain(), file.getResourcePath() + ".obj")).getInputStream();
			}
			String[] lines = IOUtils.toString(stream, Charsets.UTF_8).split("\n");
			String mtlName = null;
			Shape shape = null;
			Material currentMaterial = null;
			for(String line : lines) {
				if(line.startsWith("mtllib ")) {
					String[] arr = line.replaceFirst("mtllib ", "").split(" ");
					mtlName = arr[0];
					InputStream mtlStream;
					if(file.getResourcePath().endsWith(".obj")) {
						mtlStream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(file.getResourceDomain(), file.getResourcePath().substring(0, file.getResourcePath().lastIndexOf("/") + 1) + mtlName)).getInputStream();
					}else {
						mtlStream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(file.getResourceDomain(), file.getResourcePath() + ".mtl")).getInputStream();
					}
					System.out.println("nice");
					String materialName = null;
					Vector3f color = null;
					ResourceLocation texture = null;
					float alpha = 1;
					String[] mtlLines = IOUtils.toString(mtlStream, Charsets.UTF_8).split("\n");
					for(int i = 0; i < mtlLines.length; i++) {
						String mtlLine = mtlLines[i];
						if(mtlLine.startsWith("newmtl ")) {
							if(materialName != null && color != null) {
								Material material;
								if(texture != null) {
									material = new Material(materialName, texture, color.x, color.y, color.z, alpha);
								}else {
									material = new Material(materialName, color.x, color.y, color.z, alpha);
								}
								model.addMaterial(material);
								texture = null;
							}
							materialName = mtlLine.replaceFirst("newmtl ", "");
						}else if(mtlLine.startsWith("Kd ")) {
							String[] array = mtlLine.replaceFirst("Kd ", "").split(" ");
							color = new Vector3f(Float.parseFloat(array[0]), Float.parseFloat(array[1]), Float.parseFloat(array[2]));
						}else if(mtlLine.startsWith("d ")) {
							alpha = Float.parseFloat(mtlLine.replaceFirst("d ", ""));
						}else if(mtlLine.startsWith("map_Kd ")) {
							String string = mtlLine.replaceFirst("map_Kd ", "");
							if(string.startsWith(file.getResourceDomain() + ":")) {
								texture = new ResourceLocation(string);
							}else if(string.lastIndexOf('/') != -1) {
								texture = new ResourceLocation(file.getResourceDomain(), "textures/obj/" + string.substring(0, string.lastIndexOf('/')));
							}else {
								texture = new ResourceLocation(file.getResourceDomain(), "textures/obj/" + string);
							}
						}
						if(i == mtlLines.length - 1) {
							if(materialName != null && color != null) {
								Material material;
								if(texture != null) {
									material = new Material(materialName, texture, color.x, color.y, color.z, alpha);
								}else {
									material = new Material(materialName, color.x, color.y, color.z, alpha);
								}
								model.addMaterial(material);
								texture = null;
							}
						}
					}
					mtlStream.close();
				}
				if(line.startsWith("o ")) {
					String[] arr = line.replaceFirst("o ", "").split(" ");
					shape = new Shape(model, arr[0]);
					model.addShape(shape);
				}
				if(shape != null) {
					if(line.startsWith("v ")) {
						String[] arr = line.replaceFirst("v ", "").split(" ");
						Vertex vertex = new Vertex(Float.parseFloat(arr[0]), Float.parseFloat(arr[1]), Float.parseFloat(arr[2]));
						if(removeDuplicateVertices) {
							if(!shape.getVertices().contains(vertex)) {
								shape.addVertex(vertex);
							}
						}else {
							shape.addVertex(vertex);
						}
					}else if(line.startsWith("vt ")) {
						String[] arr = line.replaceFirst("vt ", "").split(" ");
						TextureCoords coords = new TextureCoords(Float.parseFloat(arr[0]), Float.parseFloat(arr[1]));
						shape.addTexCoords(coords);
					}else if(line.startsWith("vn ")) {
						String[] arr = line.replaceFirst("vn ", "").split(" ");
						Normal normal = new Normal(Float.parseFloat(arr[0]), Float.parseFloat(arr[1]), Float.parseFloat(arr[2]));
						shape.addNormal(normal);
					}else if(line.startsWith("usemtl ")) {
						String[] arr = line.replaceFirst("usemtl ", "").split(" ");
						currentMaterial = model.getMaterialByName(arr[0]);
					}else if(line.startsWith("f ")) {
						Face face = new Face(shape);
						String[] arr = line.replaceFirst("f ", "").split(" ");
						for(int i = 0; i < arr.length; i++) {
							Vertex vertex = null;
							TextureCoords coords = null;
							Normal normal = null;
							String[] split = arr[i].split("/");
							for(int y = 0; y < split.length; y++) {
								switch(y) {
								case 0:
									for(Vertex v : shape.getVertices()) {
										if(v.getIndex() == Integer.parseInt(split[y])) {
											vertex = v;
										}
									}
									break;
								case 1:
									for(TextureCoords tex : shape.getTextureCoords()) {
										if(tex.getIndex() == Integer.parseInt(split[y])) {
											coords = tex;
										}
									}
									break;
								case 2:
									for(Normal n : shape.getNormals()) {
										if(n.getIndex() == Integer.parseInt(split[y])) {
											normal = n;
										}
									}
									break;
								}
							}
							if(vertex != null && coords != null && normal != null) {
								face.appendWithoutParent(vertex, coords, normal);
							}
						}
						
						if(arr.length == 3) {
							ObfuscationReflectionHelper.setPrivateValue(Face.class, face, PolygonType.TRINGLE, "type");
						}else if(arr.length == 4) {
							ObfuscationReflectionHelper.setPrivateValue(Face.class, face, PolygonType.QUAD, "type");
						}else if(arr.length > 4) {
							ObfuscationReflectionHelper.setPrivateValue(Face.class, face, PolygonType.POLYGON, "type");
						}
						if(currentMaterial != null) {
							ObfuscationReflectionHelper.setPrivateValue(Face.class, face, currentMaterial, "material");
						}else {
							ObfuscationReflectionHelper.setPrivateValue(Face.class, face, defaultMaterial, "material");
						}
						shape.addFace(face);
					}
				}
			}
			stream.close();
			System.out.println(model.getMaterials());
			return model;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException n) {
			n.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Loads a file from the resource location
	 * @param file The {@link ResourceLocation} to point to the obj file
	 * @param texture The {@link ResourceLocation} to point to the obj texture file
	 * @param removeDuplicateVertices If true every duplicate vertex will not get loaded
	 * @apiNote Don't need to type extension
	 */
	public static OBJModel loadOBJModel(ResourceLocation file, ResourceLocation texture, boolean removeDuplicateVertices) {
		try {
			OBJModel model = new OBJModel();
			InputStream stream;
			if(file.getResourcePath().endsWith(".obj")) {
				stream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(file.getResourceDomain(), file.getResourcePath())).getInputStream();
			}else {
				stream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(file.getResourceDomain(), file.getResourcePath() + ".obj")).getInputStream();
			}
			String[] lines = IOUtils.toString(stream, Charsets.UTF_8).split("\n");
			String mtlName = null;
			Shape shape = null;
			Material currentMaterial = null;
			for(String line : lines) {
				if(line.startsWith("mtllib ")) {
					String[] arr = line.replaceFirst("mtllib ", "").split(" ");
					mtlName = arr[0];
					InputStream mtlStream;
					if(file.getResourcePath().endsWith(".obj")) {
						mtlStream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(file.getResourceDomain(), file.getResourcePath().substring(0, file.getResourcePath().lastIndexOf("/") + 1) + mtlName)).getInputStream();
					}else {
						mtlStream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(file.getResourceDomain(), file.getResourcePath() + ".mtl")).getInputStream();
					}
					String materialName = null;
					Vector3f color = null;
					String[] mtlLines = IOUtils.toString(mtlStream, Charsets.UTF_8).split("\n");
					for(String mtlLine : mtlLines) {
						float alpha = 1;
						if(mtlLine.startsWith("newmtl ")) {
							materialName = mtlLine.replaceFirst("newmtl ", "");
						}else if(mtlLine.startsWith("Kd ")) {
							String[] array = mtlLine.replaceFirst("Kd ", "").split(" ");
							color = new Vector3f(Float.parseFloat(array[0]), Float.parseFloat(array[1]), Float.parseFloat(array[2]));
						}else if(mtlLine.startsWith("d ")) {
							alpha = Float.parseFloat(mtlLine.replaceFirst("d ", ""));
						}
						if(materialName != null && color != null) {
							Material material;
							material = new Material(materialName, texture, color.x, color.y, color.z, alpha);
							model.addMaterial(material);
							materialName = null;
							color = null;
						}
					}
					mtlStream.close();
				}
				if(line.startsWith("o ")) {
					String[] arr = line.replaceFirst("o ", "").split(" ");
					shape = new Shape(model, arr[0]);
					model.addShape(shape);
				}
				if(shape != null) {
					if(line.startsWith("v ")) {
						String[] arr = line.replaceFirst("v ", "").split(" ");
						Vertex vertex = new Vertex(Float.parseFloat(arr[0]), Float.parseFloat(arr[1]), Float.parseFloat(arr[2]));
						if(removeDuplicateVertices && !shape.getVertices().contains(vertex)) shape.addVertex(vertex);
					}else if(line.startsWith("vt ")) {
						String[] arr = line.replaceFirst("vt ", "").split(" ");
						TextureCoords coords = new TextureCoords(Float.parseFloat(arr[0]), Float.parseFloat(arr[1]));
						shape.addTexCoords(coords);
					}else if(line.startsWith("vn ")) {
						String[] arr = line.replaceFirst("vn ", "").split(" ");
						Normal normal = new Normal(Float.parseFloat(arr[0]), Float.parseFloat(arr[1]), Float.parseFloat(arr[2]));
						shape.addNormal(normal);
					}else if(line.startsWith("usemtl ")) {
						String[] arr = line.replaceFirst("usemtl ", "").split(" ");
						currentMaterial = model.getMaterialByName(arr[0]);
					}else if(line.startsWith("f ")) {
						Face face = new Face(shape);
						String[] arr = line.replaceFirst("f ", "").split(" ");
						for(int i = 0; i < arr.length; i++) {
							Vertex vertex = null;
							TextureCoords coords = null;
							Normal normal = null;
							String[] split = arr[i].split("/");
							for(int y = 0; y < split.length; y++) {
								switch(y) {
								case 0:
									for(Vertex v : shape.getVertices()) {
										if(v.getIndex() == Integer.parseInt(split[y])) {
											vertex = v;
										}
									}
									break;
								case 1:
									for(TextureCoords tex : shape.getTextureCoords()) {
										if(tex.getIndex() == Integer.parseInt(split[y])) {
											coords = tex;
										}
									}
									break;
								case 2:
									for(Normal n : shape.getNormals()) {
										if(n.getIndex() == Integer.parseInt(split[y])) {
											normal = n;
										}
									}
									break;
								}
							}
							if(vertex != null && coords != null && normal != null) {
								face.appendWithoutParent(vertex, coords, normal);
							}
						}
						
						if(arr.length == 3) {
							ObfuscationReflectionHelper.setPrivateValue(Face.class, face, PolygonType.TRINGLE, "type");
						}else if(arr.length == 4) {
							ObfuscationReflectionHelper.setPrivateValue(Face.class, face, PolygonType.QUAD, "type");
						}else if(arr.length > 4) {
							ObfuscationReflectionHelper.setPrivateValue(Face.class, face, PolygonType.POLYGON, "type");
						}
						if(currentMaterial != null) {
							ObfuscationReflectionHelper.setPrivateValue(Face.class, face, currentMaterial, "material");
						}else {
							ObfuscationReflectionHelper.setPrivateValue(Face.class, face, defaultMaterial, "material");
						}
						shape.addFace(face);
					}
				}
			}
			stream.close();
			return model;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException n) {
			n.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Loads a file from the resource location
	 * @param file The {@link ResourceLocation} to point to the obj file
	 * @apiNote Don't need to type extension
	 */
	@Deprecated
	public static OBJModel loadOBJModel(ResourceLocation file, Object...f) {
		try {
			OBJModel model = new OBJModel();
			InputStream stream;
			if(file.getResourcePath().endsWith(".obj")) {
				stream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(file.getResourceDomain(), file.getResourcePath())).getInputStream();
			}else {
				stream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(file.getResourceDomain(), file.getResourcePath() + ".obj")).getInputStream();
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String mtlName = null;
			Shape shape = null;
			Material currentMaterial = null;
			String line;
			while((line=reader.readLine()) != null) {
				if(line.startsWith("mtllib ")) {
					String[] arr = line.replaceFirst("mtllib ", "").split(" ");
					mtlName = arr[0];
					InputStream mtlStream;
					if(file.getResourcePath().endsWith(".obj")) {
						mtlStream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(file.getResourceDomain(), file.getResourcePath().substring(0, file.getResourcePath().lastIndexOf("/") + 1) + mtlName)).getInputStream();
					}else {
						mtlStream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(file.getResourceDomain(), file.getResourcePath() + ".mtl")).getInputStream();
					}
					BufferedReader mtlReader = new BufferedReader(new InputStreamReader(mtlStream));
					String materialName = null;
					Vector3f color = null;
					String mtlLine;
					while((mtlLine=mtlReader.readLine()) != null) {
						ResourceLocation texture = null;
						float alpha = 1;
						if(mtlLine.startsWith("newmtl ")) {
							materialName = mtlLine.replaceFirst("newmtl ", "");
						}else if(mtlLine.startsWith("Kd ")) {
							String[] array = mtlLine.replaceFirst("Kd ", "").split(" ");
							color = new Vector3f(Float.parseFloat(array[0]), Float.parseFloat(array[1]), Float.parseFloat(array[2]));
						}else if(mtlLine.startsWith("d ")) {
							alpha = Float.parseFloat(mtlLine.replaceFirst("d ", ""));
						}else if(mtlLine.startsWith("map_Kd ")) {
							String string = mtlLine.replaceFirst("map_Kd ", "");
							if(string.startsWith(file.getResourceDomain() + ":")) {
								texture = new ResourceLocation(string);
							}else if(string.lastIndexOf('/') != -1) {
								texture = new ResourceLocation(file.getResourceDomain(), "textures/obj/" + string.substring(0, string.lastIndexOf('/')));
							}else {
								texture = new ResourceLocation(file.getResourceDomain(), "textures/obj/" + string);
							}
						}
						if(materialName != null && color != null) {
							Material material;
							if(texture != null) {
								material = new Material(materialName, texture, color.x, color.y, color.z, alpha);
							}else {
								material = new Material(materialName, color.x, color.y, color.z, alpha);
							}
							model.addMaterial(material);
							materialName = null;
							color = null;
						}
					}
					mtlReader.close();
				}
				if(line.startsWith("o ")) {
					String[] arr = line.replaceFirst("o ", "").split(" ");
					shape = new Shape(model, arr[0]);
					model.addShape(shape);
				}
				if(shape != null) {
					if(line.startsWith("v ")) {
						String[] arr = line.replaceFirst("v ", "").split(" ");
						Vertex vertex = new Vertex(Float.parseFloat(arr[0]), Float.parseFloat(arr[1]), Float.parseFloat(arr[2]));
						shape.addVertex(vertex);
					}else if(line.startsWith("vt ")) {
						String[] arr = line.replaceFirst("vt ", "").split(" ");
						TextureCoords coords = new TextureCoords(Float.parseFloat(arr[0]), Float.parseFloat(arr[1]));
						shape.addTexCoords(coords);
					}else if(line.startsWith("vn ")) {
						String[] arr = line.replaceFirst("vn ", "").split(" ");
						Normal normal = new Normal(Float.parseFloat(arr[0]), Float.parseFloat(arr[1]), Float.parseFloat(arr[2]));
						shape.addNormal(normal);
					}else if(line.startsWith("usemtl ")) {
						String[] arr = line.replaceFirst("usemtl ", "").split(" ");
						currentMaterial = model.getMaterialByName(arr[0]);
					}else if(line.startsWith("f ")) {
						Face face = new Face(shape);
						String[] arr = line.replaceFirst("f ", "").split(" ");
						for(int i = 0; i < arr.length; i++) {
							Vertex vertex = null;
							TextureCoords coords = null;
							Normal normal = null;
							String[] split = arr[i].split("/");
							for(int y = 0; y < split.length; y++) {
								switch(y) {
								case 0:
									for(Vertex v : shape.getVertices()) {
										if(v.getIndex() == Integer.parseInt(split[y])) {
											vertex = v;
										}
									}
									break;
								case 1:
									for(TextureCoords tex : shape.getTextureCoords()) {
										if(tex.getIndex() == Integer.parseInt(split[y])) {
											coords = tex;
										}
									}
									break;
								case 2:
									for(Normal n : shape.getNormals()) {
										if(n.getIndex() == Integer.parseInt(split[y])) {
											normal = n;
										}
									}
									break;
								}
							}
							if(vertex != null && coords != null && normal != null) {
								face.appendWithoutParent(vertex, coords, normal);
							}
						}
						
						if(arr.length == 3) {
							ObfuscationReflectionHelper.setPrivateValue(Face.class, face, PolygonType.TRINGLE, "type");
						}else if(arr.length == 4) {
							ObfuscationReflectionHelper.setPrivateValue(Face.class, face, PolygonType.QUAD, "type");
						}else if(arr.length > 4) {
							ObfuscationReflectionHelper.setPrivateValue(Face.class, face, PolygonType.POLYGON, "type");
						}
						if(currentMaterial != null) {
							ObfuscationReflectionHelper.setPrivateValue(Face.class, face, currentMaterial, "material");
						}else {
							ObfuscationReflectionHelper.setPrivateValue(Face.class, face, defaultMaterial, "material");
						}
						shape.addFace(face);
					}
				}
			}
			reader.close();
			return model;
		} catch (IOException e) {
			OrmoyoUtil.LOGGER.error("Failed to read obj file at location " + file.toString());
		} catch (NumberFormatException n) {
			OrmoyoUtil.LOGGER.error("Failed to parse obj file at location " + file.toString());
		}
		return null;
	}
}
