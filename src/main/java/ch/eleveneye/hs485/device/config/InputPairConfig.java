package ch.eleveneye.hs485.device.config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.eleveneye.hs485.device.ActorType;
import ch.eleveneye.hs485.device.SensorType;

public class InputPairConfig implements ConfigData {

	public static class InputConfig {
		private String configTitle;
		private SensorType joinType;
		private SensorType splitType;

		public InputConfig(String configTitle, SensorType joinType,
				SensorType splitType) {
			this.configTitle = configTitle;
			this.joinType = joinType;
			this.splitType = splitType;
		}

		/**
		 * @return the configTitle
		 */
		public String getConfigTitle() {
			return configTitle;
		}

		/**
		 * @return the joinType
		 */
		public SensorType getJoinType() {
			return joinType;
		}

		/**
		 * @return the splitType
		 */
		public SensorType getSplitType() {
			return splitType;
		}

		/**
		 * @param configTitle
		 *            the configTitle to set
		 */
		public void setConfigTitle(String configTitle) {
			this.configTitle = configTitle;
		}

		/**
		 * @param joinType
		 *            the joinType to set
		 */
		public void setJoinType(SensorType joinType) {
			this.joinType = joinType;
		}

		/**
		 * @param splitType
		 *            the splitType to set
		 */
		public void setSplitType(SensorType splitType) {
			this.splitType = splitType;
		}
	}

	private final Map<String, ActorType> actors;
	private int input1Type = 0;
	private int input2Type = 0;
	private InputConfig[] inputChoices;
	private final Insets DEFAULT_INSETS = new Insets(2, 2, 2, 2);

	private boolean jointInput;

	transient boolean updateRunning = false;

	public InputPairConfig(Map<String, ActorType> actors) {
		this.actors = actors;
	}

	public void appendSensors(int offset, Map<String, SensorType> ret) {
		if (jointInput) {
			ret.put((offset) + "-" + (offset + 1), inputChoices[input1Type]
					.getJoinType());
		} else {
			ret.put(Integer.toString(offset), inputChoices[input1Type]
					.getSplitType());
			ret.put(Integer.toString(offset + 1), inputChoices[input2Type]
					.getSplitType());

		}
	}

	@Override
	public ConfigData clone() throws CloneNotSupportedException {
		return (ConfigData) super.clone();
	}

	public Map<String, ActorType> connectableActors() {
		return actors;
	}

	public Map<String, SensorType> connectableSensors() {
		Map<String, SensorType> ret = new HashMap<String, SensorType>();
		appendSensors(1, ret);
		return ret;
	}

	public Map<String, ActorType> fixedActors() {
		return new TreeMap<String, ActorType>();
	}

	public Map<String, SensorType> fixedSensors() {
		return new TreeMap<String, SensorType>();
	}

	/**
	 * @return the input1Type
	 */
	public int getInput1Type() {
		return input1Type;
	}

	/**
	 * @return the input2Type
	 */
	public int getInput2Type() {
		return input2Type;
	}

	/**
	 * @return the inputChoices
	 */
	public InputConfig[] getInputChoices() {
		return inputChoices;
	}

	/**
	 * @return the jointInput
	 */
	public boolean isJointInput() {
		return jointInput;
	}

	/**
	 * @param input1Type
	 *            the input1Type to set
	 */
	public void setInput1Type(int input1Type) {
		this.input1Type = input1Type;
	}

	/**
	 * @param input2Type
	 *            the input2Type to set
	 */
	public void setInput2Type(int input2Type) {
		this.input2Type = input2Type;
	}

	/**
	 * @param inputChoices
	 *            the inputChoices to set
	 */
	public void setInputChoices(InputConfig[] inputChoices) {
		this.inputChoices = inputChoices;
	}

	/**
	 * @param jointInput
	 *            the jointInput to set
	 */
	public void setJointInput(boolean jointInput) {
		this.jointInput = jointInput;
	}

	public void showUI(JPanel panel) {
		panel.removeAll();
		panel.setLayout(new GridBagLayout());
		panel.add(new JLabel("Eingangskonfiguration"), new GridBagConstraints(
				0, 0, 1, 2, 0.1, 0.1, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, DEFAULT_INSETS, 0, 0));
		ButtonGroup joinGroup = new ButtonGroup();
		final JRadioButton joinButton = new JRadioButton("Doppeltaster");
		panel.add(joinButton, new GridBagConstraints(1, 0, 1, 1, 0.1, 0.1,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				DEFAULT_INSETS, 0, 0));
		joinGroup.add(joinButton);
		final JRadioButton splitButton = new JRadioButton("Einzeleingänge");
		panel.add(splitButton, new GridBagConstraints(1, 1, 1, 1, 0.1, 0.1,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				DEFAULT_INSETS, 0, 0));
		joinGroup.add(splitButton);

		final JLabel input1Label = new JLabel("Typ Eingang 1");
		final JLabel joinInputLabel = new JLabel("Typ Eingang");
		panel.add(input1Label, new GridBagConstraints(0, 2, 1, 1, 0.1, 0.1,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				DEFAULT_INSETS, 0, 0));
		panel.add(joinInputLabel, new GridBagConstraints(0, 2, 1, 1, 0.1, 0.1,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				DEFAULT_INSETS, 0, 0));

		Vector<String> choices = new Vector<String>(inputChoices.length);
		for (int i = 0; i < inputChoices.length; i++) {
			choices.add(inputChoices[i].getConfigTitle());
		}

		final JComboBox input1Choice = new JComboBox(choices);
		panel.add(input1Choice, new GridBagConstraints(1, 2, 1, 1, 0.1, 0.1,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				DEFAULT_INSETS, 0, 0));

		final JLabel input2Label = new JLabel("Typ Eingang 2");
		panel.add(input2Label, new GridBagConstraints(0, 3, 1, 1, 0.1, 0.1,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				DEFAULT_INSETS, 0, 0));
		final JComboBox input2Choice = new JComboBox(choices);
		panel.add(input2Choice, new GridBagConstraints(1, 3, 1, 1, 0.1, 0.1,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				DEFAULT_INSETS, 0, 0));

		final Runnable updateRunnable = new Runnable() {
			public void run() {
				if (updateRunning)
					return;
				try {
					updateRunning = true;
					input1Label.setVisible(!jointInput);
					input2Label.setVisible(!jointInput);
					joinInputLabel.setVisible(jointInput);
					input2Choice.setVisible(!jointInput);
					joinButton.setSelected(jointInput);
					splitButton.setSelected(!jointInput);

					input1Choice.setSelectedIndex(input1Type);
					input2Choice.setSelectedIndex(input2Type);
				} finally {
					updateRunning = false;
				}
			}
		};

		joinButton.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (joinButton.isSelected()) {
					jointInput = true;
					updateRunnable.run();
				}
			}
		});
		splitButton.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (splitButton.isSelected()) {
					jointInput = false;
					updateRunnable.run();
				}
			}
		});
		input1Choice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				input1Type = input1Choice.getSelectedIndex();
				updateRunnable.run();
			}
		});
		input2Choice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				input2Type = input2Choice.getSelectedIndex();
				updateRunnable.run();
			}
		});

		updateRunnable.run();
	}

}
