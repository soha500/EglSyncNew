package syncregions;

public class BoilerController {
	
	public OrderedSet<Any> execute(int temperature, int targetTemperature, boolean boilerStatus) {
		
		TemperatureController temperatureController = new TemperatureController();
		
		OrderedSet<Any> temperatureControllerResult = temperatureController.execute();
		
		BoilerActuator boilerActuator = new BoilerActuator();
		
		OrderedSet<Any> boilerActuatorResult = boilerActuator.execute();
		
		return Sequence {action}Result;
		
	}
	
}

