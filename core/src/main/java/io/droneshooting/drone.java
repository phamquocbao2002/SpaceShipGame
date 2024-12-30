package io.droneshooting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;

public class drone extends Rectangle {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private int droneIdx;
	private String image;
	private String fireImage;
	private Float fireSpeed;
	private static int fireBallSpeed = 200;
	private long lastFireTime;
	private Array<Array<Object>> fireballs;
	private Array<drone> drones;
	private int damage;
	private int hp;
	private Texture texture;
	private Texture fireballtexture;
	private Texture hpbar;
	private Texture hp_img;
	private Sound explosionSound;

	public drone(String id, Array<drone> drones) {
		super();
		this.id = id;
		setId(id);
		this.fireballs = new Array<>();
		this.drones = drones;
		if (this.id.equals("4")) {
			this.x = 800 / 2 - 200 / 2;
			this.y = 320;
			this.width = 200;
			this.height = 200;
		} else {
			this.x = (float) Math.random() * 740;
			this.y = 430;
			this.width = 40;
			this.height = 40;
		}

		this.hpbar = new Texture("hp_e1.png");
		this.hp_img = new Texture("hp_f1.png");
	}

	public Texture getTexture() {
		return texture;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public void setId(String id) {
		this.id = id;
		String line;
		FileHandle fileHandle = Gdx.files.internal("database/drone.csv");
		try (BufferedReader br = new BufferedReader(new InputStreamReader(fileHandle.read()))) {
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				if (values[0].equals(this.id)) {
					this.image = values[1];
					this.fireImage = values[2];
					this.fireSpeed = Float.parseFloat(values[3]);
					this.damage = Integer.parseInt(values[4]);
					this.hp = Integer.parseInt(values[5]);
					this.explosionSound = Gdx.audio.newSound(Gdx.files.internal(values[6]));
					this.texture = new Texture(this.image);
					this.fireballtexture = new Texture(this.fireImage);
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Sound getExplosionSound() {
		return explosionSound;
	}

	public void setExplosionSound(Sound explosionSound) {
		this.explosionSound = explosionSound;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;

	}

	public String getFireImage() {
		return fireImage;
	}

	public void setFireImage(String fireImage) {
		this.fireImage = fireImage;
		this.fireballtexture.dispose();
		this.fireballtexture = new Texture(this.fireImage);
	}

	public float getFireSpeed() {
		return fireSpeed;
	}

	public void setFireSpeed(Float fireSpeed) {
		this.fireSpeed = fireSpeed;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}


	public Array<Array<Object>> getFireballs() {
		return fireballs;
	}

	public void setFireballs(Array<Array<Object>> fireballs) {
		this.fireballs = fireballs;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public int getDroneIdx() {
		return droneIdx;
	}

	public void setDroneIdx(int droneIdx) {
		this.droneIdx = droneIdx;
	}

	public void getShot(int dam, int droneIdx, Array<item> items, Array<Array<Object>> itemsLocator) {
		this.hp -= dam;
		if (this.hp == 0) {
			dispose();
			setTexture(new Texture("explosion.png"));
			dropItem(items, itemsLocator);
			this.explosionSound.play();
			Timer.schedule(new Timer.Task() {
				@Override
				public void run() {
					if (drones.indexOf(drone.this, false) != -1) {
						drones.removeIndex(drones.indexOf(drone.this, false));
					}

				}
			}, 0.5f);
		}
	}

	public void spawnFireBall(fighter fighter) {
		if ((TimeUtils.nanoTime() - this.lastFireTime > (1000000000 / this.fireSpeed)) && hp > 0) {
			Rectangle newfireball = new Rectangle();
			if (this.id.equals("4")) {
				newfireball.width = 20;
				newfireball.height = 20;

			} else {
				newfireball.width = 5;
				newfireball.height = 5;

			}
			newfireball.x = this.x + this.width / 2 - newfireball.width / 2;
			newfireball.y = this.y;
			float vx = fireBallSpeed * (fighter.x + 40 - (newfireball.x + newfireball.width / 2)) / (this.y - 80);
			Array<Object> fa = new Array<>();
			fa.add(newfireball);
			fa.add(vx);
			fireballs.add(fa);
			lastFireTime = TimeUtils.nanoTime();
		}
	}

	public void fireBallMove(fighter fighter) {
		Iterator<Array<Object>> iter = this.fireballs.iterator();
		while (iter.hasNext()) {
			Array<Object> fa = iter.next();
			Rectangle fireball = (Rectangle) fa.get(0);
			float vx = (float) fa.get(1);
			fireball.x += vx * Gdx.graphics.getDeltaTime();
			fireball.y -= fireBallSpeed * Gdx.graphics.getDeltaTime();
			if (fireball.overlaps(fighter)) {
				fighter.getShot(damage);
				fireballs.removeIndex(fireballs.indexOf(fa, false));
			}
			if (fireball.y == 0) {
				iter.remove();
			}
		}

	}

	public void fire(fighter fighter) {
		spawnFireBall(fighter);
		fireBallMove(fighter);
	}

	public void draw(SpriteBatch batch) {
		batch.draw(texture, this.x, this.y, this.width, this.height);
		if (this.id.equals("4")) {
			batch.draw(hpbar, 375, 465, 50, 10);
			batch.draw(hp_img, 375, 466, Math.round(this.hp * 50 / 50000), 9);
		}
		Iterator<Array<Object>> iter = this.fireballs.iterator();
		while (iter.hasNext()) {
			Array<Object> fa = iter.next();
			Rectangle fireball = (Rectangle) fa.get(0);
			batch.draw(fireballtexture, fireball.x, fireball.y, fireball.width, fireball.height);
			if (fireball.y == 0) {
				iter.remove();
			}
		}
	}

	public void dropItem(Array<item> items, Array<Array<Object>> itemsLocator) {
		if (droneIdx <= 49) {
			for (int i = 0; i <= 3; i++) {
				boolean availableItem = false;
				Array<Object> itemLocations = itemsLocator.get(i);
				for (int z = 1; z <= itemLocations.size - 1; z++) {
					int itemIdx = (int) itemLocations.get(z);
					if (droneIdx == itemIdx) {
						String itemId = (String) itemLocations.get(0);
						item item = new item(itemId);
						item.x = this.x;
						item.y = this.y;
						items.add(item);
						availableItem = true;
						break;
					}
				}

				if (availableItem) {
					break;
				}
			}

		} else if (droneIdx >= 50 && droneIdx <= 99) {
			for (int i = 4; i <= 8; i++) {
				boolean availableItem = false;
				Array<Object> itemLocations = itemsLocator.get(i);
				for (int z = 1; z <= itemLocations.size - 1; z++) {
					int itemIdx = (int) itemLocations.get(z);
					if (droneIdx == itemIdx) {
						String itemId = (String) itemLocations.get(0);
						item item = new item(itemId);
						item.x = this.x;
						item.y = this.y;
						items.add(item);
						availableItem = true;
						break;
					}
				}

				if (availableItem) {
					break;
				}
			}

		} else if (droneIdx >= 100 && droneIdx <= 149) {
			for (int i = 9; i <= 13; i++) {
				boolean availableItem = false;
				Array<Object> itemLocations = itemsLocator.get(i);
				for (int z = 1; z <= itemLocations.size - 1; z++) {
					int itemIdx = (int) itemLocations.get(z);
					if (droneIdx == itemIdx) {
						String itemId = (String) itemLocations.get(0);
						item item = new item(itemId);
						item.x = this.x;
						item.y = this.y;
						items.add(item);
						availableItem = true;
						break;
					}
				}

				if (availableItem) {
					break;
				}
			}

		} else {

		}

	}

	public void dispose() {
		texture.dispose();
		fireballtexture.dispose();
	}
}
