/*
 * ome.services.blitz.omerogateway.services.impl.ImageServiceImpl 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package ome.services.blitz.gateway.services.impl;


//Java imports
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

import ome.services.blitz.gateway.services.GatewayFactory;
import ome.services.blitz.gateway.services.ImageService;
import ome.services.blitz.gateway.services.RawPixelsStoreService;
import ome.services.blitz.gateway.services.RenderingService;
import ome.services.blitz.gateway.services.ThumbnailService;
import ome.services.blitz.gateway.services.util.DataSink;
import ome.services.blitz.gateway.services.util.PixelTypes;
import ome.services.blitz.gateway.services.util.Plane2D;
import omero.RInt;
import omero.ServerError;
import omero.api.BufferedImage;
import omero.api.IPixelsPrx;
import omero.api.IQueryPrx;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsType;

import static omero.rtypes.rint;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ImageServiceImpl
	implements ImageService 
{	
	
	GatewayFactory 				gatewayFactory;
	
	/**
	 * Instantiate the imageService with the serviceFactory.
	 * @param serviceFactory see above.
	 */
	public ImageServiceImpl(GatewayFactory gatewayFactory)
	{
		this.gatewayFactory = gatewayFactory;
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#createImage(int, int, int, int, java.util.List, omero.model.PixelsType, java.lang.String, java.lang.String)
	 */
	public Long createImage(int sizeX, int sizeY, int sizeZ, int sizeT,
			List<Integer> channelList, PixelsType pixelsType, String name,
			String description) throws omero.ServerError
	{
		IPixelsPrx iPixels = gatewayFactory.getIPixels();
		return iPixels.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, name, description).getValue();
	}
	

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#copyPixels(long, int, int, int, int, java.lang.String)
	 */
	public Long copyPixels(long pixelsID, int x, int y, int t, int z, List<Integer> channelList,
			String methodology) throws omero.ServerError
	{
		IPixelsPrx iPixels = gatewayFactory.getIPixels();
		Long newID = iPixels.copyAndResizePixels
						(pixelsID, rint(x), rint(y), rint(t), rint(z), channelList, methodology, true).getValue();
		return newID;
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#copyPixels(long, java.lang.String)
	 */
	public Long copyPixels(long pixelsID, List<Integer> channelList,
			String methodology) throws omero.ServerError
	{
		IPixelsPrx iPixels = gatewayFactory.getIPixels();
		Pixels pixels = getPixels(pixelsID);
		Long newID = iPixels.copyAndResizePixels
						(pixelsID, pixels.getSizeX(), pixels.getSizeY(), 
						 pixels.getSizeT(),pixels.getSizeZ(), 
						 channelList, methodology, true).getValue();
		return newID;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#copyImage(long, int, int, int, int, java.lang.String)
	 */
	public Long copyImage(long imageId, int x, int y, int t, int z, List<Integer> channelList,
			String methodology) throws omero.ServerError
	{
		IPixelsPrx iPixels = gatewayFactory.getIPixels();
		Long newID = iPixels.copyAndResizeImage
						(imageId, rint(x), rint(y), rint(t), rint(z), channelList, methodology, true).getValue();
		return newID;
	}
	
	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.ImageService#getImage(long)
	 */
	public Image getImage(long imageID) throws ServerError
	{
		String queryStr = new String("from Image as i left outer join fetch " +
				"i.pixels as p where i.id= "+ imageID);
		IQueryPrx query = gatewayFactory.getIQuery();
		return (Image)query.findByQuery(queryStr, null);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.ImageService#getPixels(long)
	 */
	public Pixels getPixels(long pixelsId) throws ServerError
	{
		RenderingService renderingService = gatewayFactory.getRenderingService();
		return renderingService.getPixels(pixelsId);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.ImageService#getRawPlane(long, int, int, int)
	 */
	public byte[] getRawPlane(long pixelsId, int z, int c, int t)
			throws ServerError
	{
		RawPixelsStoreService rawPixelsStoreService = gatewayFactory.getRawPixelsStoreService();
		return rawPixelsStoreService.getPlane(pixelsId, z, c, t);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.ImageService#getRenderedImage(long, int, int)
	 */
	public BufferedImage getRenderedImage(long pixelsId, int z, int t)
			throws ServerError
	{
		RenderingService renderingService = gatewayFactory.getRenderingService();
		return renderingService.getRenderedImage(pixelsId, z, t);
	}	
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getRenderedImageMatrix(long, int, int)
	 */
	public int[][][] getRenderedImageMatrix(long pixelsId, int z, int t)
			throws omero.ServerError
	{
		RenderingService renderingService = gatewayFactory.getRenderingService();
		return renderingService.getRenderedImageMatrix(pixelsId, z, t);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.ImageService#getThumbnail(long, omero.RInt, omero.RInt)
	 */
	public byte[] getThumbnail(long pixelsId, RInt sizeX, RInt sizeY)
			throws ServerError
	{
		ThumbnailService thumbnailService = gatewayFactory.getThumbnailStoreService();
		return thumbnailService.getThumbnail(pixelsId, sizeX, sizeY);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.ImageService#getThumbnailSet(omero.RInt, omero.RInt, java.util.List)
	 */
	public Map<Long, byte[]> getThumbnailSet(RInt sizeX, RInt sizeY,
			List<Long> pixelsIds) throws ServerError
	{
		ThumbnailService thumbnailService = gatewayFactory.getThumbnailStoreService();
		return thumbnailService.getThumbnailSet(sizeX, sizeY, pixelsIds);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.ImageService#setActive(java.lang.Long, int, boolean)
	 */
	public void setActive(Long pixelsId, int w, boolean active)
			throws ServerError
	{
		RenderingService renderingService = gatewayFactory.getRenderingService();
		renderingService.setActive(pixelsId, w, active);
	}
	
	/**
	 * Upload the plane to the server, on pixels id with channel and the 
	 * time, + z section. the data is the client 2d data values. This will
	 * be converted to the raw server bytes.
	 * @param pixelsId pixels id to upload to .  
	 * @param z z section. 
	 * @param c channel.
	 * @param t time point.
	 * @param data plane data. 
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	public void uploadPlane(long pixelsId, int z, int c, int t, 
			double [][] data) throws omero.ServerError
	{
		RawPixelsStoreService rawPixelsStore = gatewayFactory.getRawPixelsStoreService();
		Pixels pixels = getPixels(pixelsId);
		byte[] convertedData = convertClientToServer(pixels, data);
		rawPixelsStore.setPlane(pixelsId, convertedData, z,c,t);
	}

	/**
	 * convert the client data pixels to server byte array, also sets the data
	 * pixel size to the size of the pixels in the pixels Id param.
	 * @param pixels the pixels in the server.
	 * @param data the data on the client. 
	 * @return the bytes for server.
	 */
	public byte[] convertClientToServer(Pixels pixels, double [][] data)
	{
		String pixelsType  = pixels.getPixelsType().getValue().getValue();
		int pixelsSize = getPixelsSize(pixelsType); 
		int sizex = pixels.getSizeX().getValue();
		int sizey = pixels.getSizeY().getValue();
		byte[] rawbytes =  new byte[sizex*sizey*pixelsSize];
		for ( int x = 0 ; x < sizex ; x++)
			for ( int y = 0 ; y < sizey ; y++)
			{
				int offset = calcOffset(pixelsSize, sizex, x, y);
				byte[] newBytes = convertValue(pixelsType, data[x][y]);
				for( int offsetLength = 0 ; offsetLength < newBytes.length ; offsetLength++)
					rawbytes[offset+offsetLength] = newBytes[offsetLength];  
			}
		return rawbytes;
	}

	/** 
	 * Determines the offset value.
	 * @param pixelSize pixels size in bytes. 
	 * @param x	The x-coordinate.
	 * @param y	The y-coordinate.
	 * @return See above.
	 */
	private int calcOffset(int pixelSize, int sizex, int x, int y)
	{
		return pixelSize*(y*sizex+x);
	}
	
	/**
	 * Get the pixel size of the pixels.
	 * @param pixelsType see above.
	 * @return the size in bytes. 
	 */
	private int getPixelsSize(String pixelsType)
	{	
		return PixelTypes.pixelMap.get(pixelsType);
	}

	/**
	 * Map the byte data to a byte value 
	 * @param v see above.
	 * @return see above.
	 */
	private byte[] mapToByteArray(byte v) 
	{
		ByteBuffer bb = ByteBuffer.allocate(1);
		bb.put(v);
		return bb.array();
	}

	/**
	 * Map the byte data to a byte value 
	 * @param v see above.
	 * @return see above.
	 */
	private byte[] mapToByteArray(short v) 
	{
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.putShort(v);
		return bb.array();
	}

	/**
	 * Map the byte data to a byte value 
	 * @param v see above.
	 * @return see above.
	 */
	private byte[] mapToByteArray(int v) 
	{
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(v);
		return bb.array();
	}

	/**
	 * Map the byte data to a byte value 
	 * @param v see above.
	 * @return see above.
	 */
	private byte[] mapToByteArray(float v) 
	{
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putFloat(v);
		return bb.array();
	}
	
	/**
	 * Map the byte data to a byte value 
	 * @param v see above.
	 * @return see above.
	 */
	private byte[] mapToByteArray(double v) 
	{
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putDouble(v);
		return bb.array();
	}

	/**
	 * Convert the value of the pixel in the client to the server byte value.
	 * @param pixelsType PixelType. 
	 * @param val the value to convert.
	 * @return the converted value.
	 */
	private byte[] convertValue(String pixelsType, Double val)
	{
		if(pixelsType.equals(PixelTypes.INT_8) || pixelsType.equals(PixelTypes.UINT_8))
			return mapToByteArray(val.byteValue());
		else if(pixelsType.equals(PixelTypes.INT_16) || pixelsType.equals(PixelTypes.UINT_16))
			return mapToByteArray(val.shortValue());
		else if(pixelsType.equals(PixelTypes.INT_32) || pixelsType.equals(PixelTypes.UINT_32))
			return mapToByteArray(val.intValue());
		else if(pixelsType.equals(PixelTypes.FLOAT))
			return mapToByteArray(val.floatValue());
		else
			return mapToByteArray(val.doubleValue());
	}
	

	/**
	 * Convert the rawPlane to doubles depending on the endianess and type of
	 * values in the rawplane.
	 * @param pixels The pixels data of the plane.
	 * @param z The z section.
	 * @param c The channel.
	 * @param t The time point.
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	private double[][] convertRawPlaneAsDouble(Pixels pixels, int z, int c, int t) 
		throws omero.ServerError 
	{
		DataSink sink = DataSink.makeNew(pixels, this);
		Plane2D plane = sink.getPlane(z, t, c);
		return plane.getPixelsArrayAsDouble();
	}

	/**
	 * Convert the rawPlane to doubles depending on the endianess and type of
	 * values in the rawplane.
	 * @param pixels The pixels data of the plane.
	 * @param z The z section.
	 * @param c The channel.
	 * @param t The time point.
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	private long[][] convertRawPlaneAsLong(Pixels pixels, int z, int c, int t) 
		throws omero.ServerError 
	{
		DataSink sink = DataSink.makeNew(pixels, this);
		Plane2D plane = sink.getPlane(z, t, c);
		return plane.getPixelsArrayAsLong();
	}
	
	/**
	 * Convert the rawPlane to doubles depending on the endianess and type of
	 * values in the rawplane.
	 * @param pixels The pixels data of the plane.
	 * @param z The z section.
	 * @param c The channel.
	 * @param t The time point.
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	private int[][] convertRawPlaneAsInt(Pixels pixels, int z, int c, int t) 
		throws omero.ServerError 
	{
		DataSink sink = DataSink.makeNew(pixels, this);
		Plane2D plane = sink.getPlane(z, t, c);
		return plane.getPixelsArrayAsInt();
	}

	/**
	 * Convert the rawPlane to doubles depending on the endianess and type of
	 * values in the rawplane.
	 * @param pixels The pixels data of the plane.
	 * @param z The z section.
	 * @param c The channel.
	 * @param t The time point.
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	private short[][] convertRawPlaneAsShort(Pixels pixels, int z, int c, int t) 
		throws omero.ServerError 
	{
		DataSink sink = DataSink.makeNew(pixels, this);
		Plane2D plane = sink.getPlane(z, t, c);
		return plane.getPixelsArrayAsShort();
	}
	
	/**
	 * Convert the rawPlane to doubles depending on the endianess and type of
	 * values in the rawplane.
	 * @param pixels The pixels data of the plane.
	 * @param z The z section.
	 * @param c The channel.
	 * @param t The time point.
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	private byte[][] convertRawPlaneAsByte(Pixels pixels, int z, int c, int t) 
		throws omero.ServerError 
	{
		DataSink sink = DataSink.makeNew(pixels, this);
		Plane2D plane = sink.getPlane(z, t, c);
		return plane.getPixelsArrayAsByte();
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getRawPlane(long, int, int, int)
	 */
	public double[][] getPlaneAsDouble(long pixelsId, int z, int c, int t) 
		throws omero.ServerError
	{
		Pixels pixels = getPixels(pixelsId);
		return convertRawPlaneAsDouble(pixels, z, c, t);
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getRawPlane(long, int, int, int)
	 */
	public long[][] getPlaneAsLong(long pixelsId, int z, int c, int t) 
		throws omero.ServerError
	{
		Pixels pixels = getPixels(pixelsId);
		return convertRawPlaneAsLong(pixels, z, c, t);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getRawPlane(long, int, int, int)
	 */
	public int[][] getPlaneAsInt(long pixelsId, int z, int c, int t) 
		throws omero.ServerError
	{
		Pixels pixels = getPixels(pixelsId);
		return convertRawPlaneAsInt(pixels, z, c, t);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getRawPlane(long, int, int, int)
	 */
	public short[][] getPlaneAsShort(long pixelsId, int z, int c, int t) 
		throws omero.ServerError
	{
		Pixels pixels = getPixels(pixelsId);
		return convertRawPlaneAsShort(pixels, z, c, t);
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getRawPlane(long, int, int, int)
	 */
	public byte[][] getPlaneAsByte(long pixelsId, int z, int c, int t) 
		throws omero.ServerError
	{
		Pixels pixels = getPixels(pixelsId);
		return convertRawPlaneAsByte(pixels, z, c, t);
	}

	
	
}

