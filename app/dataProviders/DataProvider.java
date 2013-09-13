package util;

/*
//------------------------------------------------------------------------------
class ModelRunner
{
	RegisteredDataProviders
	RegisteredModels
	
	ModelRunRequest
		For Each ModelRunRequest
			Get Required DataProviders
}

//------------------------------------------------------------------------------
class Model
{
	Requires:
		DataProviders (or none)
}
*/

// TODO: Seems like there needs to be some synchronization of height/width + current
//	x,y cell processing between models and data providers? 
//	Maybe not that big of a deal but seems like not ensuring this sort of sync'ing
//	might be asking for problems? Such as, if a model isn't coded properly to work with
//	the assumptions of how a provider serves up data, well...that is bad.
//
//------------------------------------------------------------------------------
public abstract class DataProvider
{
	// Output
	protected float[][] mProcessedLine;

	public enum EDataProvider {
		EDP_CornGrass_Provider,
		EDP_390m_Window_Provider
	}	

	public abstract EDataProvider getProviderType();
	
	public void init() { /*does nothing unless overridden*/ }
	public abstract float[][] getLine(int[][] rotationData); // MUST override
	public void done() { /*does nothing unless overridden*/ }
}

