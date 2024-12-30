package io.droneshooting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class item extends Rectangle {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String image;
	private String interactiveAttribute;
	private float value;
	private int fallenSpeed;
	private Texture texture;
	public item(String id) {
		super();
		setId(id);
		this.width = 40;
		this.height = 40;
	}
	
	public void setId(String id) {
		this.id = id;
		String line;
		FileHandle fileHandle = Gdx.files.internal("database/item.csv");
		try (BufferedReader br = new BufferedReader(new InputStreamReader(fileHandle.read()))) {
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				if (values[0].equals(id)) {
					this.image = values[1];
					this.interactiveAttribute = values[2];
					this.value = Float.parseFloat(values[3]);
					this.fallenSpeed = Integer.parseInt(values[4]);
					this.texture = new Texture(this.image);
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	
	public String getInteractiveAttribute() {
		return interactiveAttribute;
	}
	public void setInteractiveAttribute(String interactiveAttribute) {
		this.interactiveAttribute = interactiveAttribute;
	}
	public float getValue() {
		return value;
	}
	public void setValue(float value) {
		this.value = value;
	}
	public int getFallenSpeed() {
		return fallenSpeed;
	}
	public void setFallenSpeed(int fallenSpeed) {
		this.fallenSpeed = fallenSpeed;
	}
	
	public void move(Array<item> items, fighter fighter) {
		this.y -= fallenSpeed * Gdx.graphics.getDeltaTime();
		if (this.overlaps(fighter)) {
			rewardFighter(fighter);
			items.removeIndex(items.indexOf(item.this, false));
		} else {
			if (this.y <= 0) {
				items.removeIndex(items.indexOf(item.this, false));
			}
		}
	}
	
	public Texture getTexture() {
		return texture;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}
	
	public void rewardFighter(fighter fighter) {
		if (id == "1") {
			fighter.upgradeBarrel();
			
		} else if (id == "2") {
			fighter.upgradeFiringSpeed(Math.round(value));
			
		} else if (id == "3") {
			fighter.heal(value);
			
		} else if (id == "4") {
//			fighter.upgradeDamage(Math.round(value));
		} else {
			fighter.levelup();
		}
	}
	
}
