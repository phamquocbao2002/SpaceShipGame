package io.spaceship;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Main extends ApplicationAdapter {
	FitViewport viewport;
	private int FPS = 240;
	private long lastFrameTime = TimeUtils.nanoTime();
	private SpriteBatch batch;
	private Array<drone> drones;
	private int droneIdx = 0;
	private Array<item> items;
	private long lastSpawnDroneTime;
	private Stage stage;
	private Skin skin;
	private TextButton replay;
	private BitmapFont customFont;
	private fighter fighter;
	private Texture background;
	private Texture fighter_hpbar;
	private Texture fighter_hp;
	private Array<Array<Object>> itemsLocator = new Array<>();
	private boolean win = false;

	@Override
	public void create() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = 100;
		parameter.color = Color.RED;
		parameter.borderWidth = 3;
		parameter.borderColor = Color.BLACK;
		parameter.shadowOffsetX = 5;
		parameter.shadowOffsetY = 5;
		parameter.shadowColor = Color.GRAY;
		viewport = new FitViewport(800, 480);
		customFont = generator.generateFont(parameter);
		generator.dispose();
		batch = new SpriteBatch();
		fighter = new fighter("1");
		background = new Texture("background/space_bk.png");
		fighter_hpbar = new Texture("hp_e1.png");
		fighter_hp = new Texture("hp_f1.png");
		drones = new Array<>();
		items = new Array<>();
		itemsLocator = new Array<>();
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);
		skin = new Skin(Gdx.files.internal("uiskin.json"));
		skin.add("default-font", customFont);
		skin.add("default", new Label.LabelStyle(customFont, Color.RED));
		replay = new TextButton("Play again!", skin);
		replay.setSize(120, 30);
		replay.setPosition(340, 200);
		replay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fighter = new fighter("1");
				rePlay();
			}
		});
		stage.addActor(replay);
	}

	private void spawnDrone() {
		drone drone = null;
		if (droneIdx <= 49) {
			drone = new drone("1", drones);
			lastSpawnDroneTime = TimeUtils.millis();
		} else if (droneIdx >= 50 && droneIdx <= 99) {
			drone = new drone("2", drones);
			lastSpawnDroneTime = TimeUtils.millis();
		} else if (droneIdx >= 100 && droneIdx <= 149) {
			drone = new drone("3", drones);
			lastSpawnDroneTime = TimeUtils.millis();
		} else if (droneIdx == 150) {
			drone = new drone("4", drones);
		}
		if (drone != null) {
			drone.setDroneIdx(droneIdx);
			drones.add(drone);
			droneIdx++;
		}
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
	}

	@Override
	public void render() {
		logic();
		if (TimeUtils.nanoTime() - lastFrameTime > (1000000000 / FPS)) {
			lastFrameTime = TimeUtils.nanoTime();
			draw();
		}
	}

	public void draw() {

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		viewport.apply();
		batch.setProjectionMatrix(viewport.getCamera().combined);
		batch.begin();
		batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
		batch.draw(fighter_hpbar, 10, 450, 100, 20);
		batch.draw(fighter_hp, 10, 451, Math.round(fighter.getHp() * 100 / fighter.getMaxHp()), 18);
		batch.draw(fighter.getTexture(), fighter.x, fighter.y, fighter.width, fighter.height);

		fighter.draw(batch);
		for (item item : items) {
			batch.draw(item.getTexture(), item.x, item.y, item.width, item.height);
		}
		for (drone drone : drones) {

			drone.draw(batch);

		}
		batch.end();

		if (fighter.getHp() <= 0) {
			drawGameLost();
		}

		if (win) {
			drawGameWin();
		}
	}

	public void logic() {
		if (itemsLocator.size == 0) {
			itemsGenerator();
		}

		fighter.move();

		for (item item : items) {
			item.move(items, fighter);
		}

		if (TimeUtils.millis() - lastSpawnDroneTime > 1000) {
			if (fighter.getEnemiesDestroyed() <= 150) {
				spawnDrone();
			} else {
				win = true;
			}
		}

		for (drone drone : drones) {
			drone.fire(fighter);
		}

		fighter.fire(drones, items, itemsLocator);

		if (fighter.getHp() <= 0) {
			gameOver();
		}

	}

	private void drawGameWin() {
		Label gameWin = new Label("You Win!", skin);
		fighter.getFireballs().clear();
		gameWin.setSize(500, 150);
		gameWin.setPosition(150, 240);
		stage.addActor(gameWin);
		stage.act();
		stage.draw();

	}

	public void drawGameLost() {
		Label gameOver = new Label("You Lost!", skin);
		gameOver.setSize(500, 150);
		gameOver.setPosition(150, 240);
		stage.addActor(gameOver);
		stage.act();
		stage.draw();
	}

	public void gameOver() {
		for (drone drone : drones) {
			drone.getFireballs().clear();
		}
		droneIdx = 0;
		fighter.getFireballs().clear();
		itemsLocator.clear();
		items.clear();
		win = false;
	}

	public void rePlay() {
		droneIdx = 0;
		drones.clear();
		fighter.getFireballs().clear();
		itemsLocator.clear();
		items.clear();
		win = false;
		spawnDrone();
	}

	public void itemsGenerator() {
		Random random = new Random();
		Array<Object> l11 = new Array<>();
		Array<Object> l12 = new Array<>();
		Array<Object> l13 = new Array<>();
		Array<Object> l14 = new Array<>();
		Array<Object> l21 = new Array<>();
		Array<Object> l22 = new Array<>();
		Array<Object> l23 = new Array<>();
		Array<Object> l24 = new Array<>();
		Array<Object> l25 = new Array<>();
		Array<Object> l31 = new Array<>();
		Array<Object> l32 = new Array<>();
		Array<Object> l33 = new Array<>();
		Array<Object> l34 = new Array<>();
		Array<Object> l35 = new Array<>();

		Set<Integer> l1Idxs = new HashSet<>();
		Array<Integer> l1Idxs_f = new Array<>();
		Set<Integer> l2Idxs = new HashSet<>();
		Array<Integer> l2Idxs_f = new Array<>();
		Set<Integer> l3Idxs = new HashSet<>();
		Array<Integer> l3Idxs_f = new Array<>();

		while (l1Idxs.size() < 7) {
			boolean existed = false;
			int number = random.nextInt(50);
			try {
				l1Idxs.add(number);
			} catch (Exception e) {
				existed = true;
			}

			if (!existed) {
				l1Idxs_f.add(number);
			}
		}

		while (l2Idxs.size() < 8) {
			boolean existed = false;
			int number = random.nextInt(50, 100);
			try {
				l2Idxs.add(number);
			} catch (Exception e) {
				existed = true;
			}

			if (!existed) {
				l2Idxs_f.add(number);
			}
		}

		while (l3Idxs.size() < 8) {
			boolean existed = false;
			int number = random.nextInt(100, 150);
			try {
				l3Idxs.add(number);
			} catch (Exception e) {
				existed = true;
			}

			if (!existed) {
				l3Idxs_f.add(number);
			}
		}

		l1Idxs.clear();
		l2Idxs.clear();
		l3Idxs.clear();

		l11.add("1", l1Idxs_f.get(0));
		l12.add("2", l1Idxs_f.get(1), l1Idxs_f.get(2), l1Idxs_f.get(3));
		l13.add("3", l1Idxs_f.get(4), l1Idxs_f.get(5));

		l21.add("1", l2Idxs_f.get(0));
		l22.add("2", l2Idxs_f.get(1), l2Idxs_f.get(2));
		l23.add("3", l2Idxs_f.get(3), l2Idxs_f.get(4), l2Idxs_f.get(5));
		l25.add("5", l2Idxs_f.get(6), l2Idxs_f.get(7));

		l31.add("1", l3Idxs_f.get(0));
		l32.add("2", l3Idxs_f.get(1), l3Idxs_f.get(2));
		l33.add("3", l3Idxs_f.get(3), l3Idxs_f.get(4), l3Idxs_f.get(5));
		l35.add("5", l3Idxs_f.get(7));

		itemsLocator.add(l11);
		itemsLocator.add(l12);
		itemsLocator.add(l13);
		itemsLocator.add(l14);

		itemsLocator.add(l21);
		itemsLocator.add(l22);
		itemsLocator.add(l23);
		itemsLocator.add(l24);
		itemsLocator.add(l25);

		itemsLocator.add(l31);
		itemsLocator.add(l32);
		itemsLocator.add(l33);
		itemsLocator.add(l34);
		itemsLocator.add(l35);

	}

	@Override
	public void dispose() {
		batch.dispose();
		stage.dispose();
		skin.dispose();
		customFont.dispose();
		fighter.dispose();
		for (drone d : drones) {
			d.dispose();
		}
		drones.clear();

	}
}
