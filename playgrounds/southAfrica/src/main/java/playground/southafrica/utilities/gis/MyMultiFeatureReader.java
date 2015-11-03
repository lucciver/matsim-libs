/* *********************************************************************** *
 * project: org.matsim.*
 * MyGapReader.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.southafrica.utilities.gis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import playground.southafrica.utilities.containers.MyZone;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class MyMultiFeatureReader {

	private List<MyZone> zones;
	private final Logger log = Logger.getLogger(MyMultiFeatureReader.class);
	
	
	public MyMultiFeatureReader(){

	}
	
	
	/**
	 * Reads multizone shapefiles.
	 * @param shapefile
	 * @param idField indicates which field to use for the {@link Id} of the zone. Known fields include:
	 * <ul>
	 * 	<li>Geospatial Analysis Platform (GAP) shapefiles:
	 * 	<ul>
	 * 		<li> Gauteng: 1
	 * 		<li> KwaZulu-Natal: 2
	 * 		<li> Western Cape: 2
	 * 	</ul>
	 * </ul>
	 * @throws IOException if the shapefile does not exist, or is not readable.
	 */
	public void readMultizoneShapefile (String shapefile, int idField) throws IOException{
		File f = new File(shapefile);
		if(!f.exists() || !f.canRead()){
			throw new IOException("Cannot read from " + shapefile);
		}
		log.info("Start reading shapefile " + shapefile);

		this.zones = new ArrayList<MyZone>();
		MultiPolygon mp = null;
		GeometryFactory gf = new GeometryFactory();
		Collection<SimpleFeature> features = ShapeFileReader.getAllFeatures(shapefile);
		for (SimpleFeature feature : features) {
			String name = String.valueOf( feature.getAttribute( idField ) ); 
			Object shape = feature.getDefaultGeometry();
			if( shape instanceof MultiPolygon ){
				mp = (MultiPolygon)shape;
				if( !mp.isSimple() ){
					log.warn("This polygon is NOT simple!" );
				}
				Polygon polygonArray[] = new Polygon[mp.getNumGeometries()];
				for(int j = 0; j < mp.getNumGeometries(); j++ ){
					if(mp.getGeometryN(j) instanceof Polygon ){
						polygonArray[j] = (Polygon) mp.getGeometryN(j);							
					} else{
						log.warn("Subset of multipolygon is NOT a polygon.");
					}
				}
				MyZone newZone = new MyZone(polygonArray, gf, new IdImpl(name));

				this.zones.add( newZone );
			} else{
				log.warn("This is not a multipolygon!");
			}
		}
		log.info("Done reading shapefile.");
	}

	public MyZone getZone(Id id){
		MyZone result = null;
		for (MyZone aZone : this.zones) {
			if(aZone.getId().equals(id)){
				result = aZone;
			}
		}
		if(result == null){
			String errorMessage = "Could not find the requested zone: " + id.toString();
			log.error(errorMessage);
		}
		return result;
	}
	
	
	public List<MyZone> getAllZones() {
		return this.zones;
	}
	
	
	/**
	 * Reads a point feature shapefile.
	 * @return {@link List} of {@link Point}s.
	 */
	public List<Point> readPoints(String shapefile) {
		List<Point> list = new ArrayList<Point>();
		for(SimpleFeature feature : ShapeFileReader.getAllFeatures(shapefile)){
			Geometry geo = (Geometry) feature.getDefaultGeometry();
			if(geo instanceof Point){
				Point ps = (Point)geo;
				
				for(int i = 0; i < ps.getNumGeometries(); i++){
					Point p = (Point) ps.getGeometryN(i);
					list.add(p);
				}
			} else{
				throw new RuntimeException("The shapefile does not contain Point(s)!");
			}
		}
		return list;
	}


	/**
	 * Reads a point feature shapefile.
	 * @return {@link List} of {@link Coord}s.
	 */
	public List<Coord> readCoords(String shapefile) {
		List<Coord> list = new ArrayList<Coord>();
		for(SimpleFeature feature: ShapeFileReader.getAllFeatures(shapefile)){
			Geometry geo = (Geometry) feature.getDefaultGeometry();
			if(geo instanceof Point){
				Point ps = (Point)geo;
				list.add(new CoordImpl(ps.getX(), ps.getY()));
			} else{
				throw new RuntimeException("The shapefile does not contain Point(s)!");
			}
		}
		return list;
	}


}