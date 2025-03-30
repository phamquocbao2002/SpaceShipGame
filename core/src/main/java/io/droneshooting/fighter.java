package io.droneshooting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class fighter extends Rectangle {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String image;
	private static int speed = 500;
	private int barrels = 1;
	private String fireImage;
	private int fireSpeed;
	private Array<Rectangle> fireballs;
	private static int fireBallSpeed = 1500;
	private long lastFireTime;
	private int damage;
	private int maxHp;
	private int hp;
	private Texture texture;
	private Texture fireballtexture;
	private int enemiesDestroyed = 0;
	private Sound damageSound;
	private Sound explosionSound;
	private Texture damEffect;
	private boolean getShot = false;

	public fighter(String id) {
		super();
		setId(id);
		this.fireballs = new Array<>();
		this.x = 800 / 2 - 80 / 2;
		this.y = 0;
		this.width = 80;
		this.height = 80;
		this.damageSound = Gdx.audio.newSound(Gdx.files.internal("damageSound.mp3"));
		this.explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.wav"));
		this.damEffect = new Texture("damEffect2.png");

	}


	public void setId(String id) {
		this.id = id;
		String line;
		FileHandle fileHandle = Gdx.files.internal("database/fighter.csv");
		try (BufferedReader br = new BufferedReader(new InputStreamReader(fileHandle.read()))) {
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				if (values[0].equals(id)) {
					this.image = values[1];
					this.fireImage = values[2];
					this.fireSpeed = Integer.parseInt(values[3]);
					this.damage = Integer.parseInt(values[4]);
					float hpRate = 1;
					if (this.maxHp != 0) {
						hpRate = (float) this.hp / this.maxHp;
					}
					this.maxHp = Integer.parseInt(values[5]);
					this.hp = (int) Math.round(this.maxHp * hpRate);
					this.texture = new Texture(this.image);
					this.fireballtexture = new Texture(this.fireImage);
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Sound getDamageSound() {
		
		return damageSound;
	}

	public void setDamageSound(Sound damageSound) {
		this.damageSound = damageSound;
	}

	public Sound getExplosionSound() {
		return explosionSound;
	}

	public void setExplosionSound(Sound explosionSound) {
		this.explosionSound = explosionSound;
	}

	public String getId() {
		return id;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
		this.texture.dispose();
		this.texture = new Texture("explosion.png");
	}

	public String getFireImage() {
		return fireImage;
	}

	public Array<Rectangle> getFireballs() {
		return fireballs;
	}

	public void setFireballs(Array<Rectangle> fireballs) {
		this.fireballs = fireballs;
	}

	public long getLastFireTime() {
		return lastFireTime;
	}

	public void setLastFireTime(long lastFireTime) {
		this.lastFireTime = lastFireTime;
	}

	public void setFireImage(String fireImage) {
		this.fireImage = fireImage;
	}

	public int getFireSpeed() {
		return fireSpeed;
	}

	public void setFireSpeed(int fireSpeed) {
		this.fireSpeed = fireSpeed;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public void getShot(int dam) {
		getShot = true;
		this.hp -= dam;
		this.damageSound.setVolume(this.damageSound.play(), 0.5f);
		if (this.hp <= 0) {
			setImage("explosion.png");
		}
	}

	public Texture getFireballtexture() {
		return fireballtexture;
	}

	public void setFireballtexture(Texture fireballtexture) {
		this.fireballtexture = fireballtexture;
	}

	public Texture getTexture() {
		return texture;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public void move() {
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			this.x -= speed * Gdx.graphics.getDeltaTime();
		}

		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			this.x += speed * Gdx.graphics.getDeltaTime();
		}

		if (this.x < 0)
			this.x = 0;
		if (this.x > 730)
			this.x = 730;
	}

	public void spawnFireBall() {
		if (TimeUtils.nanoTime() - this.lastFireTime > (1000000000 / this.fireSpeed)) {
			Rectangle fireball_center = new Rectangle();
			fireball_center.x = this.x + 34;
			fireball_center.y = 80;
			fireball_center.width = 12;
			fireball_center.height = 36;
			if (barrels == 1) {
				fireballs.add(fireball_center);
			} else {
				Rectangle fireball_right = new Rectangle();
				fireball_right.x = this.x + 34 + 20;
				fireball_right.y = 80 - 40;
				fireball_right.width = 14;
				fireball_right.height = 36;

				Rectangle fireball_left = new Rectangle();
				fireball_left.x = this.x + 34 - 20;
				fireball_left.y = 80 - 40;
				fireball_left.width = 12;
				fireball_left.height = 36;

				fireballs.add(fireball_center);
				fireballs.add(fireball_right);
				fireballs.add(fireball_left);
			}

			lastFireTime = TimeUtils.nanoTime();
		}
	}

	public void fireBallMove(Array<drone> drones, Array<item> items, Object[] itemsLocator) {
		Iterator<Rectangle> iter1 = this.fireballs.iterator();
		while (iter1.hasNext()) {
			Rectangle fireball = iter1.next();
			fireball.y += fireBallSpeed * Gdx.graphics.getDeltaTime();
			if (fireball.y >= 430) {
				if (fireball.y >= 480) {
					iter1.remove();
				} else {
					Iterator<drone> drs = drones.iterator();
					while (drs.hasNext()) {
						drone drone = drs.next();
						if (fireball.overlaps(drone)) {
							drone.getShot(damage, this.getEnemiesDestroyed(), items, itemsLocator);
							if (fireballs.indexOf(fireball, false) != -1) {
								fireballs.removeIndex(fireballs.indexOf(fireball, false));
							}
							if (drone.getHp() == 0) {
								setEnemiesDestroyed(getEnemiesDestroyed() + 1);
							}
						}
					}

				}
			}
		}
	}

	public void fire() {
		spawnFireBall();
	}

	public void draw(SpriteBatch batch) {
		batch.draw(texture, this.x, this.y, this.width, this.height);
		Iterator<Rectangle> iter1 = this.fireballs.iterator();
		while (iter1.hasNext()) {
			Rectangle fireball = iter1.next();
			batch.draw(fireballtexture, fireball.x, fireball.y, fireball.width, fireball.height);
		}

		if (this.getShot) {
			batch.draw(this.damEffect, this.x - 10, 0, 100, 100);
			getShot = false;
		}
	}

	public int getEnemiesDestroyed() {
		return enemiesDestroyed;
	}

	public void setEnemiesDestroyed(int enemiesDestroyed) {
		this.enemiesDestroyed = enemiesDestroyed;
	}

	public int getBarrels() {
		return barrels;
	}

	public void setBarrels(int barrels) {
		this.barrels = barrels;
	}

	public int getMaxHp() {
		return maxHp;
	}

	public void upgradeBarrel() {
		setBarrels(getBarrels() + 2);
	}

	public void upgradeFiringSpeed(int value) {
		setFireSpeed(getFireSpeed() + value);
	}

	public void heal(float healingRate) {
		int healedHp = this.hp + Math.round(this.maxHp * healingRate);
		if (healedHp > this.maxHp) {
			healedHp = this.maxHp;
		}
		setHp(healedHp);
	}

	public void upgradeDamage(int value) {
		setDamage(getDamage() + value);
	}

	public void levelup() {
		setId(String.valueOf(Integer.valueOf(getId()) + 1));
	}

	public void dispose() {
		this.texture.dispose();
		this.fireballtexture.dispose();
		this.damEffect.dispose();
	}
	
}
