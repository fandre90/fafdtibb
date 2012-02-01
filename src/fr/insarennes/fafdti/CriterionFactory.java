package fr.insarennes.fafdti;

import java.util.Map;

public class CriterionFactory {
	private Map<String, MakeCriterionCommand> commandMap;
	private Map<String, Criterion> instanceMap;
	private Map<Class, String> classMap;

	public static final CriterionFactory INSTANCE = new CriterionFactory();

	private CriterionFactory() {
		this.registerCriterion("E", EntropyCriterion.class,
				new MakeCriterionCommand() {

					@Override
					public Criterion makeCriterion() {
						return new EntropyCriterion();
					}
				});
	}
	
	public String getCodeLetter(Class criterionClass) {
		if(!this.classMap.containsKey(criterionClass)) {
			throw new IllegalArgumentException(
					"Criterion Class" + criterionClass + "isn't registered" +
					"in the criterion factory");
		}
		return this.classMap.get(criterionClass);
	}

	public Criterion makeCriterion(String codeLetter) {
		if(this.instanceMap.containsKey(codeLetter)) {
			return instanceMap.get(codeLetter);
		}
		if(!this.commandMap.containsKey(codeLetter)) {
			throw new IllegalArgumentException("Code letter " + codeLetter
					+ "doesn't correspond to any registered criterion");
		}
		MakeCriterionCommand cmd = this.commandMap.get(codeLetter);
		Criterion wantedCriterion = cmd.makeCriterion();
		this.instanceMap.put(codeLetter, wantedCriterion);
		return wantedCriterion;
	}

	public void registerCriterion(String codeLetter, Class criterionClass,
			MakeCriterionCommand cmd) {
		commandMap.put(codeLetter, cmd);
		classMap.put(criterionClass, codeLetter);
	}
}
