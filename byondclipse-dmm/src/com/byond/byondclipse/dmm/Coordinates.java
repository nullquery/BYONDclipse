package com.byond.byondclipse.dmm;

public class Coordinates
{
	private final int x, y, z;

	public Coordinates(final int x, final int y, final int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override public boolean equals(final Object obj)
	{
		if (obj instanceof Coordinates)
		{
			final Coordinates c								= (Coordinates) obj;

			return c.x == this.x && c.y == this.y && c.z == this.z;
		}
		else												{ return false; }
	}

	@Override public int hashCode()							{ return -1; }
	@Override public String toString()						{ return this.x + "," + this.y + "," + this.z; }
}