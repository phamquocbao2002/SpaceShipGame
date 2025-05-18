package io.droneshooting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
	private static int FPS = 240;
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
	private Texture mainBg1;
	private Texture fighter_hpbar;
	private Texture fighter_hp;
	private Object[] itemsLocator = {};
	private boolean win = false;
	private boolean lose = false;
	private TextButton start;
	private boolean started = false;
	private int bgY = 0;
	private TextButton exist;

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
		mainBg1 = new Texture("background/bg2.png");
		fighter = new fighter("1");
		fighter_hpbar = new Texture("hp_e1.png");
		fighter_hp = new Texture("hp_f1.png");
		drones = new Array<>();
		items = new Array<>();
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);
		skin = new Skin(Gdx.files.internal("uiskin.json"));
		skin.add("default-font", customFont);
		skin.add("default", new Label.LabelStyle(customFont, Color.RED));
		replay = new TextButton("Play again !", skin);
		replay.setSize(120, 30);
		replay.setPosition((viewport.getWorldWidth() - replay.getWidth()) / 2, 200);
		replay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				rePlay();
			}
		});

		start = new TextButton("Start !", skin);
		start.setSize(120, 30);
		start.setPosition((viewport.getWorldWidth() - start.getWidth()) / 2, 200);
		start.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				gameStart();
			}
		});

		exist = new TextButton("Exist !", skin);
		exist.setSize(120, 30);
		exist.setPosition((viewport.getWorldWidth() - exist.getWidth()) / 2, 160);
		exist.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				exist();
			}

			private void exist() {
				// TODO Auto-generated method stub
				Gdx.app.exit();
			}
		});
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

		if (!win && !lose) {
			logic();
			if (TimeUtils.nanoTime() - lastFrameTime > (1000000000 / FPS)) {
				lastFrameTime = TimeUtils.nanoTime();
				if (started) {
					drawGamePlay();
				} else {
					drawGameStart();
				}

			}
		} else if (win) {
			drawGameWin();
		} else if (lose) {
			drawGameLost();
		}

	}

	public void logic() {

		if (started && !win && !false) {
			gamePlay();
		} else {
			stayRoom();
		}

	}

	private void drawGameStart() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();

		Label gameStart = new Label("Click To Start !", skin);
		fighter.getFireballs().clear();
		gameStart.setPosition((viewport.getWorldWidth() - gameStart.getWidth()) / 2, 240);

		stage.addActor(gameStart);
		stage.addActor(start);
		stage.addActor(exist);
		stage.act();
		stage.draw();
		batch.end();
	}

	private void drawGamePlay() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		viewport.apply();
		batch.setProjectionMatrix(viewport.getCamera().combined);
		batch.begin();
		setBackgroud();
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
	}

	private void setBackgroud() {

		this.bgY -= Math.round(480 * Gdx.graphics.getDeltaTime());
		batch.draw(this.mainBg1, 0, 480 + this.bgY, viewport.getWorldWidth(), viewport.getWorldHeight());
		batch.draw(this.mainBg1, 0, this.bgY, viewport.getWorldWidth(), viewport.getWorldHeight());
		if (this.bgY <= -480) {
			this.bgY = 0;
		}

	}

	private void drawGameWin() {
		batch.begin();
		Label gameWin = new Label("You Win !", skin);
		fighter.getFireballs().clear();
		gameWin.setPosition((viewport.getWorldWidth() - gameWin.getWidth()) / 2, 240);
		stage.addActor(gameWin);
		stage.addActor(replay);
		stage.addActor(exist);
		stage.act();
		stage.draw();
		batch.end();
	}

	public void drawGameLost() {
		batch.begin();
		Label gameOver = new Label("Game Over !", skin);
		gameOver.setPosition((viewport.getWorldWidth() - gameOver.getWidth()) / 2, 240);
		stage.addActor(gameOver);
		stage.addActor(replay);
		stage.addActor(exist);
		stage.act();
		stage.draw();
		batch.end();
	}

	private void stayRoom() {

	}

	private void gamePlay() {
		if (itemsLocator.length == 0) {
			itemsGenerator();
		}

		fighter.move();

		for (item item : items) {
			item.move(items, fighter);
		}

		if (TimeUtils.millis() - lastSpawnDroneTime > 1000) {
			if (win == false && lose == false) {
				spawnDrone();
			}

			if (fighter.getEnemiesDestroyed() == 151) {
				gameWin();
			}
		}

		for (drone drone : drones) {
			drone.fire(fighter);
		}

		if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
			fighter.fire();
		}

		fighter.fireBallMove(drones, items, itemsLocator);

		if (fighter.getHp() <= 0) {
			fighter.setImage("explosion.png");
			gameOver();
		}
	}

	public void gameStart() {
		started = true;
	}

	public void gameWin() {
		droneIdx = 0;
		fighter.getFireballs().clear();
		itemsLocator = new Object[] {};
		items.clear();
		win = true;
		stage.clear();
	}

	public void gameOver() {
		for (drone drone : drones) {
			drone.getFireballs().clear();
		}
		droneIdx = 0;
		fighter.getFireballs().clear();
		itemsLocator = new Object[] {};
		items.clear();
		lose = true;
		stage.clear();
	}

	public void rePlay() {
		droneIdx = 0;
		drones.clear();
		fighter.getFireballs().clear();
		fighter = new fighter("1");
		itemsLocator = new Object[] {};
		items.clear();
		lose = false;
		win = false;
		spawnDrone();
		stage.clear();
	}

	public void itemsGenerator() {
		Random random = new Random();
		Set<Integer> stage1_items_indexes = new HashSet<>();
		Set<Integer> stage2_items_indexes = new HashSet<>();
		Set<Integer> stage3_items_indexes = new HashSet<>();

		while (stage1_items_indexes.size() <= 6) {
			int number = random.nextInt(50);
			try {
				stage1_items_indexes.add(number);
			} catch (Exception e) {

			}
		}

		while (stage2_items_indexes.size() <= 8) {
			int number = random.nextInt(50, 100);
			try {
				stage2_items_indexes.add(number);
			} catch (Exception e) {

			}
		}

		while (stage3_items_indexes.size() <= 9) {
			int number = random.nextInt(100, 150);
			try {
				stage3_items_indexes.add(number);
			} catch (Exception e) {

			}
		}

		List<Integer> stage1_items_indexes_array = new ArrayList<>(stage1_items_indexes);
		List<Integer> stage2_items_indexes_array = new ArrayList<>(stage2_items_indexes);
		List<Integer> stage3_items_indexes_array = new ArrayList<>(stage3_items_indexes);

		Object[] stage1_items_type1 = { "1", stage1_items_indexes_array.get(0) };
		Object[] stage1_items_type2 = { "2", stage1_items_indexes_array.get(1), stage1_items_indexes_array.get(2),
				stage1_items_indexes_array.get(3) };
		Object[] stage1_items_type3 = { "3", stage1_items_indexes_array.get(4), stage1_items_indexes_array.get(5) };
		
		Object[] stage2_items_type1 = { "1", stage2_items_indexes_array.get(0) };
		Object[] stage2_items_type2 = { "2", stage1_items_indexes_array.get(1), stage1_items_indexes_array.get(2) };
		Object[] stage2_items_type3 = { "3", stage2_items_indexes_array.get(3), stage2_items_indexes_array.get(4),
				stage2_items_indexes_array.get(5), stage2_items_indexes_array.get(6) };
		Object[] stage2_items_type5 = { "5", stage2_items_indexes_array.get(7) };
		
		Object[] stage3_items_type1 = { "1", stage3_items_indexes_array.get(0) };
		Object[] stage3_items_type2 = { "2", stage1_items_indexes_array.get(1), stage1_items_indexes_array.get(2) };
		Object[] stage3_items_type3 = { "3", stage3_items_indexes_array.get(3), stage3_items_indexes_array.get(4),
				stage3_items_indexes_array.get(5), stage3_items_indexes_array.get(6) };
		Object[] stage3_items_type5 = { "5", stage3_items_indexes_array.get(7), stage3_items_indexes_array.get(8) };

		itemsLocator = new Object[] { stage1_items_type1, stage1_items_type2, stage1_items_type3,
				stage2_items_type1, stage2_items_type2, stage2_items_type3, stage2_items_type5, stage3_items_type1,
				stage3_items_type2, stage3_items_type3, stage3_items_type5 };

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
