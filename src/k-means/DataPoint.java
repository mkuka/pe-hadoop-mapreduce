import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.Writable;


public class DataPoint implements Writable {
	int dimention;
	private float[] observations =null ;
	private int numPoints; 
	float x=0;
	float[] y=null;
	
	public DataPoint() {		
	}
		
	public DataPoint(float[] obs) {
		this.setObservations(obs);		
	}

	public DataPoint(String[] obs) {
		this.set(obs);
	}
		
	public void set(String[] obs) {
		this.dimention=obs.length;
		this.observations=new float[this.dimention];
		this.numPoints=1;

		for(int i=0;i<dimention;i++) {
			obs[i].split(",");
			this.observations[i]=Float.parseFloat(obs[i]); 
		}
		this.setXY();		
	}
	
	public float[] getObservations() {
		return observations;
	}

	public void setObservations(float[] observations) {
		this.dimention=observations.length; 
		this.observations=new float[this.dimention];
		for(int i=0;i<dimention;i++) { 
			this.observations[i]=observations[i];
		}
		this.setXY();
		this.numPoints=1;
	}
			
	public int getDimention() {
		return dimention;
	}

	public void setDimention(int dimention) {
		this.dimention = dimention;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float[] getY() {
		return y;
	}
	
	public void setXY() {
		this.y=new float[dimention-1];
		for(int i=1;i<dimention;i++){
			this.y[i-1]=this.observations[i];
		}
		this.x=observations[0];
	}

	public void setY(float[] y) {			
		this.y = y;
	}

	public String toString() {
		StringBuilder point = new StringBuilder();
		for (int i = 0; i < this.dimention; i++) {
			point.append(Float.toString(this.observations[i]));
			if(i != dimention - 1) {
				point.append(",");
			}   
		}
		return point.toString();
	}
		
	public float findDist(DataPoint c) {
		float distance=0;
		if (this.dimention!=c.dimention) {
			return -1;
		}
		else {
			float dep=0;			
			for(int i=0;i<this.y.length;i++) {
				dep+=Math.pow((this.y[i]-c.y[i]),2);				
			}
		distance = (float)(Math.sqrt((float)(Math.pow((this.x-c.x),2))+dep));
		return distance;
		}		 
	}
	
	public void average() {
		for (int i = 0; i < this.dimention; i++) {
			float temp = this.observations[i] / this.numPoints;
			this.observations[i] = (float)Math.round(temp*100000)/100000.0f;
		}
		this.numPoints = 1;
	}
		 
	public static DataPoint copy(final DataPoint p) {
		DataPoint d = new DataPoint(p.observations);
		d.numPoints = p.numPoints;
		return d;
	}
		 
	public void sum(DataPoint p) {
		for (int i = 0; i < this.dimention; i++) {
			this.observations[i] += p.observations[i];
		}
		this.numPoints += p.numPoints;
	}

	@Override
	public void readFields(DataInput arg0) throws IOException {
	    this.dimention = arg0.readInt();
	    this.numPoints = arg0.readInt();
	    this.observations = new float[this.dimention];
		for(int i = 0; i < this.dimention; i++) {
			this.observations[i] = arg0.readFloat();
		}		
	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		arg0.writeInt(this.dimention);
		arg0.writeInt(this.numPoints);
		for(int i = 0; i < this.dimention; i++) {
			arg0.writeFloat(this.observations[i]);
		}	
	}		
}


	
	


