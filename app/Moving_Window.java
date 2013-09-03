package util;

// This class calculate proportation of Ag, forest and grass with the user specified rectangle buffer 
// Inputs are location of cell, window size
// Output proporation of Ag, Forest, Grass

// Define working variables

// Version 08/20/2013

public class Moving_Window
{
	public int ULX, ULY, LRX, LRY, Total;
	
	public float Prop_Ag;
	public int Count_Ag;
	public float Prop_Forest;
	public int Count_Forest;
	public float Prop_Grass;
	public int Count_Grass;
	public float[] Proportion = new float[3]; 
	
	// Define moving window function
	
	public Moving_Window(int x, int y, int wsz, int w, int h)
	{
		// Step 1: Locate corners of window
		
		ULX = x - wsz/2;
		ULY = y - wsz/2;
		LRX = x + wsz/2;
		LRY = y + wsz/2;
		
		// Left
		if (ULX < 0)
		{
			ULX = 0;
		}
		// Up
		if (ULY < 0)
		{
			ULY = 0;
		}
		// Right
		if (LRX > w - 1)
		{
			LRX = w - 1;
		}
		// Low
		if (LRY > h - 1)
		{
			LRY = h - 1;
		}

		Total = 0;	
	}
	
	// Moving window function
	
	public float[] Window_Operation(int[][] RotationT)
	{
		
		Total = 0;
		
		// Land cover masks
		
		int Ag_Mask = 1 + 2 + 4 + 8 + 16 + 32 + 64 + 512; // 1, 2, 3, 4, 5, 6, 7, 10
		int Forest_Mask = 1024; // 11
		int Grass_Mask = 128 + 256; // 8 and 9
		
		// I to Width and J to Height
		for (int j = ULY; j <= LRY; j++) 
		{
			for (int i = ULX; i <= LRX; i++) 
			{
				if (RotationT[j][i] != 0)
				{
					Total++;
					// Calculate count of land cover in the given moving window
					// Ag
					if ((RotationT[j][i] & Ag_Mask) > 0)
					{
						Count_Ag = Count_Ag + 1;	
					}
					// Forest
					else if ((RotationT[j][i] & Forest_Mask) > 0 )
					{
						Count_Forest = Count_Forest + 1;
					}
					// Grass
					else if ((RotationT[j][i] & Grass_Mask) > 0 )
					{
						Count_Grass = Count_Grass + 1;
					}
				}
			}
		}
		
		// Calculate Proportion of land cover in the given moving window
		// Agriculture Proportion
		Prop_Ag = (float)Count_Ag / Total;
		// Forest Proportion
		Prop_Forest = (float)Count_Forest / Total;
		// Grass Proportion
		Prop_Grass = (float)Count_Grass / Total;
						
		// Concatenate three outputs to one array
		Proportion[0] = Prop_Ag;
		Proportion[1] = Prop_Forest;
		Proportion[2] = Prop_Grass;
		
		return Proportion;

	}
}
