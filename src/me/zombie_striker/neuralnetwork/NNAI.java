package me.zombie_striker.neuralnetwork;

/**
 Copyright (C) 2017  Zombie_Striker

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **/

import java.util.*;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import me.zombie_striker.neuralnetwork.neurons.*;

public class NNAI implements ConfigurationSerializable {

	public static int idTotal = 0;
	private int id = 0;

	public NNBaseEntity entity;

	private HashMap<Integer, Neuron> allNeurons = new HashMap<>();
	private List<Layer> layers = new ArrayList<Layer>();

	private int currentNeuronId = 0;
	private int currentTick = -1;
	public int MAX_LAYERS = -1;

	public NNAI(NNBaseEntity nnEntityBase) {
		this(nnEntityBase, 3);
	}

	public NNAI(NNBaseEntity nnEntityBase, int layers_amount) {
		id = idTotal;
		idTotal++;
		entity = nnEntityBase;
		nnEntityBase.ai = this;

		this.MAX_LAYERS = layers_amount;

		for (int i = 0; i < MAX_LAYERS; i++) {
			layers.add(new Layer(i));
		}
	}

	public NNAI(NNBaseEntity e, int id, boolean addToTotal) {
		this(e, id, addToTotal, 3);
	}
	

	public NNAI(NNBaseEntity e, int id, boolean addToTotal, int layers_amount) {
		this.id = id;
		if (addToTotal)
			idTotal++;
		entity = e;
		e.ai = this;

		this.MAX_LAYERS = layers_amount;

		for (int i = 0; i < MAX_LAYERS; i++) {
			layers.add(new Layer(i));
		}
	}

	public void setNeuronsPerRow(int row, int amount) {
		this.getLayer(row).setNeuronsPerRow(amount);
	}

	public int getNeuronsPerRow(int row) {
		return this.getLayer(row).getNeuronsPerRow();
	}
	public Neuron getNeuronFromId(Integer n) {
		return allNeurons.get(n);
	}

	public Set<Neuron> getNeuronsFromId(Collection<Integer> set) {
		Set<Neuron> neurons = new HashSet<Neuron>();
		for (Integer n : set) {
			neurons.add(allNeurons.get(n));
		}
		return neurons;
	}

	public static NNAI generateAI(NNBaseEntity nnEntityBase,
			int numberOfMotorNeurons, String... names) {
		NNAI ai = new NNAI(nnEntityBase);
		for (int i = 0; i < numberOfMotorNeurons; i++) {
			OutputNeuron omn = new OutputNeuron(ai, i);
			if (i < names.length)
				omn.setName(names[i]);
		}
		return ai;
	}

	public static NNAI generateAI(NNBaseEntity nnEntityBase,
			int numberOfMotorNeurons, int layers, String... names) {
		NNAI ai = new NNAI(nnEntityBase, layers);
		for (int i = 0; i < numberOfMotorNeurons; i++) {
			OutputNeuron omn = new OutputNeuron(ai, i);
			if (i < names.length)
				omn.setName(names[i]);
		}
		return ai;
	}

	public boolean[] think() {
		this.tick();
		for(Layer l : this.layers){
			for(Neuron n: l.neuronsInLayer){
				n.forceTriggerStengthUpdate();
			}
		}
		boolean[] points = new boolean[getOutputNeurons().size()];
		for (Neuron n : getOutputNeurons()) {
			if (n.isTriggered()) {
				if (n instanceof OutputNeuron)
					points[((OutputNeuron) n).responceid] = true;
			}
		}
		return points;
	}
	
	public NNAI clone(NNBaseEntity base){
		NNAI ai = new NNAI(base,MAX_LAYERS);
		for(int i = 0; i < allNeurons.size();i++){
			allNeurons.get(i).generateNeuron(ai);
		}
		return ai;
	}

	public void tick() {
		currentTick++;
	}

	public List<Layer> getLayers() {
		return layers;
	}

	public Layer getLayer(int layer) {
		return layers.get(layer);
	}

	public List<Neuron> getInputNeurons() {
		return layers.get(0).neuronsInLayer;
	}
	public List<Neuron> getAllNeurons() {
		return new ArrayList<Neuron>(allNeurons.values());
	}

	public void addNeuron(Neuron n) {
		this.allNeurons.put(n.getID(), n);
	}

	
	public int getCurrentTick() {
		return currentTick;
	}

	public void setCurrentTick(int i) {
		currentTick = i;
	}

	public int generateNeuronId() {
		return currentNeuronId++;
	}

	public int getNewestId() {
		return currentNeuronId;
	}

	public ArrayList<Neuron> getNeuronsInLayer(int layer) {
		return layers.get(layer).neuronsInLayer;
	}

	public ArrayList<Neuron> getOutputNeurons() {
		return getNeuronsInLayer(layers.size() - 1);
	}
	@SuppressWarnings("unchecked")
	public NNAI(Map<String, Object> map) {
		this.layers = (List<Layer>) map.get("l");
		for (Layer l : layers) {
			for (Neuron n : l.neuronsInLayer) {
				if (n != null) {
					n.setAI(this);
					addNeuron(n);
					//getNeuronsInLayer(n.layer).add(n);
				}
			}
		}
		this.MAX_LAYERS = (int) map.get("ml");
		this.id = (int) map.get("id");
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("l", this.layers);
		m.put("ml", this.MAX_LAYERS);
		m.put("id", this.id);
		return m;
	}

}
