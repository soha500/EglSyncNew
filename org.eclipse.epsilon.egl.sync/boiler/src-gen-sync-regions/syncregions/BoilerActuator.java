package syncregions;

public class BoilerActuator {
	
	public int execute(int temperatureDifference, boolean boilerStatus) {

		//sync _bfpnGUbFEeqXnfGWlV2_8A, code     			 

		if (temperatureDifference > 0 && boilerStatus == true) {
			return 1; // turn off boiler
			System.out.println("Hi");
		}
		else if (temperatureDifference < 0 && boilerStatus == false) {
			return 2; // turn on boiler
		}
		else return 0; // do nothing
		//endSync
	}
	
}