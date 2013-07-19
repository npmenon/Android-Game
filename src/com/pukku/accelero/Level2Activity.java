package com.pukku.accelero;

import java.io.IOException;
import java.util.ArrayList;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import android.graphics.Color;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

public class Level2Activity extends BaseGameActivity implements
		IAccelerationListener {

	// ---------------------------------------------
	// CONSTANTS
	// ---------------------------------------------

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	private static int COIN_COLUMN = 4;
	private static int COIN_ROWS = 2;

	private static final String groundID = "ground";
	private static final String heroID = "hero";
	private static final String knifeID = "knife";
	private static final String rollerID = "roller";
	private static final String finishID = "finish";
	private static final String coinID = "coin";

	private static final int startingY = 40;
	private static final int endingY = CAMERA_HEIGHT - 72;
	private static final int startingUpY = 430;
	private static final int endingUpY = 40;

	private static int POPUP_COLUMNS = 2;
	private static int POP_ROWS = 2;

	private static final int COIN_SCORE = 50;
	
	private static final String TAG = "GameActivity";
	
	static private Sound mGameOver, mGrenade, mStab, mApplause, mMunch;

	// ---------------------------------------------
	// VARIABLES
	// ---------------------------------------------

	private SharedPreferences scoresOptions;
	private SharedPreferences.Editor scoresEditor;
	
	
	private SharedPreferences audioOptions;

	private Text coinscoreText, showScoreText;
	private String YourScoreIs, CurrentScore;
	private int coinScoreAdd = 0;
	private int highScores;
	private Scene scoreScene;

	private int direcDown;
	private int direcUp;

	private Camera gameCamera;
	private Handler mHandler;
	private Scene gameBackground, gameOverScene, popUp;

	private Sprite knife, upknife;
	private Body knifeDownbody, knifeUpbody;	
	
	PhysicsWorld level2PhysicsWorld;
	private JSONObject groundObject, heroObject, knifeObject, rollerObject,
			finishObject, coinObject;
	private ArrayList<JSONObject> deletejsonList;

	// ---------------------------------------------
	// TEXTURES & TEXTURE REGIONS
	// ---------------------------------------------

	private BitmapTextureAtlas mGameBackGroundTexture, FlubberTexture,
			knifeTexture, rollerTexture, coinTexture, gameOverTexture,
			finishTexture, popUpTexture;
	private ITextureRegion mGameBackGroundTextureRegion, FlubberTextureRegion,
			knifeTextureRegion, gameOverTextureRegion, finishTextureRegion;
	private TiledTextureRegion rollerTextureRegion, popUpTextureRegion,
			coinTextureRegion;

	private ITexture mFontTexture;
	private Font mFont;

	// ---------------------------------------------
	// CLASS LOGIC
	// ---------------------------------------------

	@Override
	public EngineOptions onCreateEngineOptions() {
		// TODO Auto-generated method stub

		deletejsonList = new ArrayList<JSONObject>();

		scoresOptions = getSharedPreferences("scores", MODE_PRIVATE);
		scoresEditor = scoresOptions.edit();
		highScores = scoresOptions.getInt("Level2", 0);
		scoresEditor.commit();

		audioOptions = getSharedPreferences("audio", MODE_PRIVATE);
		mHandler = new Handler();
		gameCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		EngineOptions gameOptions = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), this.gameCamera);
		gameOptions.getAudioOptions().setNeedsSound(true);
		return gameOptions;
	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {
		// TODO Auto-generated method stub

		this.mFontTexture = new BitmapTextureAtlas(this.getTextureManager(),
				256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		FontFactory.setAssetBasePath("font/");
		mFont = FontFactory.createFromAsset(this.getFontManager(),
				this.mFontTexture, this.getAssets(), "texas.ttf", 72.0f, true,
				Color.GREEN);
		mFont.load();

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		popUpTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024,
				512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		popUpTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(popUpTexture, this.getAssets(),
						"newwin.png", 0, 0, POPUP_COLUMNS, POP_ROWS);
		popUpTexture.load();

		gameOverTexture = new BitmapTextureAtlas(this.getTextureManager(), 512,
				512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		gameOverTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(gameOverTexture, this.getAssets(),
						"gameover4.png", 0, 0);
		gameOverTexture.load();

		finishTexture = new BitmapTextureAtlas(this.getTextureManager(), 64,
				64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		finishTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(finishTexture, this, "finish.png", 0, 0);
		finishTexture.load();

		Log.i(TAG, "Loading background image");
		mGameBackGroundTexture = new BitmapTextureAtlas(
				this.getTextureManager(), 128, 128,
				TextureOptions.REPEATING_BILINEAR_PREMULTIPLYALPHA);
		mGameBackGroundTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mGameBackGroundTexture, this.getAssets(),
						"parket.png", 0, 0);
		mGameBackGroundTexture.load();
		Log.i(TAG, "Loading background image");

		Log.i(TAG, "Loading flubber image");
		FlubberTexture = new BitmapTextureAtlas(this.getTextureManager(), 64,
				64);
		FlubberTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(FlubberTexture, this, "player.png", 0, 0);
		FlubberTexture.load();
		Log.i(TAG, "Loading flubber image");

		Log.i(TAG, "Loading knifedown image");
		knifeTexture = new BitmapTextureAtlas(this.getTextureManager(), 32,
				128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		knifeTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(knifeTexture, this.getAssets(), "knifeud.png",
						0, 0);
		knifeTexture.load();
		Log.i(TAG, "Loading knifedown image");

		rollerTexture = new BitmapTextureAtlas(this.getTextureManager(), 128,
				128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		rollerTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(rollerTexture, this.getAssets(),
						"roller.png", 0, 0, 2, 1);
		rollerTexture.load();

		coinTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 64,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		coinTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(coinTexture, this.getAssets(),
						"coinrotate.png", 0, 0, COIN_COLUMN, COIN_ROWS);
		coinTexture.load();

		gameOverTexture = new BitmapTextureAtlas(this.getTextureManager(), 512,
				512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		gameOverTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(gameOverTexture, this.getAssets(),
						"gameover4.png", 0, 0);
		gameOverTexture.load();

		SoundFactory.setAssetBasePath("mfx/");

		try {
			Level2Activity.mGameOver = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), getApplicationContext(),
					"game_over.ogg");
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		try {
			Level2Activity.mGrenade = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), getApplicationContext(),
					"grenade.ogg");
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		try {
			Level2Activity.mStab = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), getApplicationContext(),
					"stab.ogg");
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		try {
			Level2Activity.mApplause = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), getApplicationContext(),
					"applause.ogg");
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		try {
			Level2Activity.mMunch = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), getApplicationContext(),
					"munch.ogg");
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		// TODO Auto-generated method stub

		gameBackground = new Scene();

		gameOverScene = new Scene();
		gameOverScene.setBackground(new Background(0, 0, 0));

		popUp = new Scene();
		popUp.setBackground(new Background(0, 0, 0));

		scoreScene = new Scene();
		scoreScene.setBackground(new Background(0, 0, 0));

		YourScoreIs = "Your Score is:~ ";
		coinscoreText = new Text(0, 0, mFont, YourScoreIs,
				this.getVertexBufferObjectManager());
		coinscoreText.setPosition(10,
				(CAMERA_HEIGHT - coinscoreText.getHeight()) / 2);
		scoreScene.attachChild(coinscoreText);

		CurrentScore = new String();
		showScoreText = new Text(0, 0, mFont, this.CurrentScore,
				(this.CurrentScore + "XXXX" + "New High Score!!").length(),
				this.getVertexBufferObjectManager());
		showScoreText.setPosition(coinscoreText.getWidth() + 15,
				(CAMERA_HEIGHT - coinscoreText.getHeight()) / 2);
		scoreScene.getLastChild().attachChild(showScoreText);

		level2PhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		createWalls();

		mGameBackGroundTextureRegion.setTextureWidth(CAMERA_WIDTH);
		mGameBackGroundTextureRegion.setTextureHeight(CAMERA_HEIGHT);
		Sprite background = new Sprite(0, 0, mGameBackGroundTextureRegion,
				this.getVertexBufferObjectManager());
		this.gameBackground.attachChild(background);

		Sprite gameOver = new Sprite(0, 0, gameOverTextureRegion,
				this.getVertexBufferObjectManager());
		gameOver.setPosition((CAMERA_WIDTH - gameOver.getWidth()) / 2,
				CAMERA_HEIGHT - gameOver.getHeight() - 80);
		this.gameOverScene.attachChild(gameOver);

		Sprite background1 = new Sprite(0, 0, mGameBackGroundTextureRegion,
				this.getVertexBufferObjectManager());
		this.popUp.attachChild(background1);

		this.gameBackground.registerUpdateHandler(level2PhysicsWorld);
		pOnCreateSceneCallback.onCreateSceneFinished(gameBackground);
	}

	private void createWalls() {
		// TODO Auto-generated method stub
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2,
				CAMERA_WIDTH, 2, this.getVertexBufferObjectManager());
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2,
				this.getVertexBufferObjectManager());
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT,
				this.getVertexBufferObjectManager());
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2,
				CAMERA_HEIGHT, this.getVertexBufferObjectManager());

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0,
				0.5f, 0.5f);

		Body WALL_GROUND = PhysicsFactory.createBoxBody(level2PhysicsWorld,
				ground, BodyType.StaticBody, wallFixtureDef);
		groundObject = makeJsonData(groundID, WALL_GROUND, ground);
		WALL_GROUND.setUserData(groundObject);

		Body WALL_ROOF = PhysicsFactory.createBoxBody(level2PhysicsWorld, roof,
				BodyType.StaticBody, wallFixtureDef);
		groundObject = makeJsonData(groundID, WALL_ROOF, roof);
		WALL_ROOF.setUserData(groundObject);

		Body WALL_LEFT = PhysicsFactory.createBoxBody(level2PhysicsWorld, left,
				BodyType.StaticBody, wallFixtureDef);
		groundObject = makeJsonData(groundID, WALL_LEFT, left);
		WALL_LEFT.setUserData(groundObject);

		Body WALL_RIGHT = PhysicsFactory.createBoxBody(level2PhysicsWorld,
				right, BodyType.StaticBody, wallFixtureDef);
		groundObject = makeJsonData(groundID, WALL_RIGHT, right);
		WALL_RIGHT.setUserData(groundObject);

		Entity rectangleGroup = new Entity();
		rectangleGroup.attachChild(ground);
		rectangleGroup.attachChild(roof);
		rectangleGroup.attachChild(left);
		rectangleGroup.attachChild(right);

		rectangleGroup.setAlpha(0.0f);
		this.gameBackground.attachChild(rectangleGroup);
	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		// TODO Auto-generated method stub

		AnimatedSprite pop = new AnimatedSprite(0, 0, this.popUpTextureRegion,
				this.getVertexBufferObjectManager());
		pop.setPosition((CAMERA_WIDTH - pop.getWidth()) / 2,
				(CAMERA_HEIGHT - pop.getHeight()) / 2);
		this.popUp.getLastChild().attachChild(pop);
		pop.animate(150);

		Sprite finish = new Sprite(0, 0, finishTextureRegion,
				this.getVertexBufferObjectManager());
		finish.setPosition(CAMERA_WIDTH - finish.getWidth(), 8);
		final Body finishBody = PhysicsFactory.createBoxBody(
				level2PhysicsWorld, finish, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		level2PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				finish, finishBody, true, false));
		finishObject = makeJsonData(finishID, finishBody, finish);
		finishBody.setUserData(finishObject);
		this.gameBackground.getLastChild().attachChild(finish);

		Sprite flubber = new Sprite(0, 0, FlubberTextureRegion,
				this.getVertexBufferObjectManager());
		flubber.setPosition(2, CAMERA_HEIGHT - flubber.getHeight());
		final FixtureDef fixDe = PhysicsFactory.createFixtureDef(0.5f, 0.5f,
				0.2f);
		final Body flubberBody = PhysicsFactory.createCircleBody(
				level2PhysicsWorld, flubber, BodyType.DynamicBody, fixDe);
		level2PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				flubber, flubberBody, true, false));
		heroObject = makeJsonData(heroID, flubberBody, flubber);
		flubberBody.setUserData(heroObject);
		this.gameBackground.getLastChild().attachChild(flubber);

		addCoin(5, 5);
		addCoin(688, 445);

		addRoller(165, 270);
		addCoin(235, 330);
		addCoin(197, 405);

		addRoller(20, 120);

		addRoller(175, 20);
		addCoin(245, 84);
		addCoin(207, 160);

		addRoller(530, 20);
		addRoller(530, 270);

		knife = new Sprite(0, 0, knifeTextureRegion,
				this.getVertexBufferObjectManager());
		knife.setPosition(CAMERA_WIDTH / 2 + 50, 44);
		final FixtureDef fixDef = PhysicsFactory.createFixtureDef(0, 0, 0);
		knifeDownbody = PhysicsFactory.createBoxBody(level2PhysicsWorld, knife,
				BodyType.KinematicBody, fixDef);
		knifeObject = makeJsonData(knifeID, knifeDownbody, knife);
		knifeDownbody.setUserData(knifeObject);
		knifeDownbody.setLinearVelocity(0, (40) / 32);
		knifeDownbody.setBullet(true);
		direcDown = 1;
		level2PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(knife,
				knifeDownbody, true, false));
		this.gameBackground.registerUpdateHandler(this.setDownVelocity());
		this.gameBackground.getLastChild().attachChild(knife);

		upknife = new Sprite(0, 0, knifeTextureRegion,
				this.getVertexBufferObjectManager());
		upknife.setPosition(CAMERA_WIDTH / 2 + 10, 400);
		final FixtureDef fixDeff = PhysicsFactory.createFixtureDef(0, 0, 0);
		knifeUpbody = PhysicsFactory.createBoxBody(level2PhysicsWorld, upknife,
				BodyType.KinematicBody, fixDeff);
		knifeObject = makeJsonData(knifeID, knifeUpbody, upknife);
		knifeUpbody.setUserData(knifeObject);
		knifeUpbody.setBullet(true);
		knifeUpbody.setLinearVelocity(0, -40 / 32);
		direcUp = 2;
		level2PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				upknife, knifeUpbody, true, false));
		this.gameBackground.registerUpdateHandler(this.setUpVelocity());
		this.gameBackground.getLastChild().attachChild(upknife);

		this.level2PhysicsWorld.setContinuousPhysics(true);
		
		this.level2PhysicsWorld.setContactListener(new ContactListener() {

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				// TODO Auto-generated method stub

			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
				// TODO Auto-generated method stub

			}

			@Override
			public void endContact(Contact contact) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beginContact(Contact contact) {
				// TODO Auto-generated method stub
				Body bodyA = contact.getFixtureA().getBody();
				Body bodyB = contact.getFixtureB().getBody();

				JSONObject obA = (JSONObject) bodyA.getUserData();
				JSONObject obB = (JSONObject) bodyB.getUserData();

				try {

					if ((obA.get("ID").equals(heroID) && obB.get("ID").equals(
							rollerID))
							|| (obB.get("ID").equals(heroID) && obA.get("ID")
									.equals(rollerID))) {

						if (obA.get("ID").equals(heroID)
								&& obB.get("ID").equals(rollerID)) {

							bodyA.setType(BodyType.StaticBody);

						} else {

							bodyB.setType(BodyType.StaticBody);
						}

						if (audioOptions.getBoolean("effectsOn", true)) {

							Level2Activity.mGrenade.play();
							mHandler.postDelayed(mPlayGameOverSound, 2000);
							mHandler.postDelayed(mTry, 3500);
						}

						else {
							mEngine.setScene(gameOverScene);
							mHandler.postDelayed(mTry, 1500);
						}

					} else if ((obA.get("ID").equals(heroID) && obB.get("ID")
							.equals(knifeID))
							|| (obB.get("ID").equals(heroID) && obA.get("ID")
									.equals(knifeID))) {

						final Sprite spA = (Sprite) obA.get("sprite");
						final Sprite spB = (Sprite) obB.get("sprite");

						spA.clearUpdateHandlers();
						spB.clearUpdateHandlers();

						if (obA.get("ID").equals(heroID)
								&& obB.get("ID").equals(knifeID)) {

							bodyA.setType(BodyType.StaticBody);

							bodyB.setType(BodyType.StaticBody);

						} else {

							bodyB.setType(BodyType.StaticBody);

							bodyA.setType(BodyType.StaticBody);
						}

						if (audioOptions.getBoolean("effectsOn", true)) {

							Level2Activity.mStab.play();
							mHandler.postDelayed(mPlayGameOverSound, 2100);
							mHandler.postDelayed(mTry, 3500);
						}

						else {
							mEngine.setScene(gameOverScene);
							mHandler.postDelayed(mTry, 1500);
						}

					} else if ((obA.get("ID").equals(heroID) && obB.get("ID")
							.equals(finishID))
							|| (obB.get("ID").equals(heroID) && obA.get("ID")
									.equals(finishID))) {

						if (obA.get("ID").equals(heroID)
								&& obB.get("ID").equals(finishID)) {

							bodyA.setType(BodyType.StaticBody);

						} else if (obB.get("ID").equals(heroID)
								&& obA.get("ID").equals(finishID)) {

							bodyB.setType(BodyType.StaticBody);

						}

						final String scorenow;
						scorenow = Integer.toString(scoresOptions.getInt(
								"score", 0));

						if (highScores < scoresOptions.getInt("score", 0)) {
							scoresEditor.putInt("Level1", highScores);
							scoresEditor.commit();
						}
						showScoreText.setText(scorenow);
						// If current score is greater than high score

						if (audioOptions.getBoolean("effectsOn", true)) {

							Level2Activity.mApplause.play();
							// background.clearChildScene();
							mEngine.setScene(popUp);
							mHandler.postDelayed(mCoinSceneAdd, 3500);
							mHandler.postDelayed(mEndGame, 6500);
						} else {
							mEngine.setScene(popUp);
							mHandler.postAtTime(mCoinSceneAdd, 3500);
							mHandler.postAtTime(mEndGame, 6500);
						}

					} else if ((obA.get("ID").equals(heroID) && obB.get("ID")
							.equals(coinID))
							|| (obB.get("ID").equals(heroID) && obA.get("ID")
									.equals(coinID))) {

						Log.i(TAG,
								"entered contact method between coin and hero");

						coinScoreAdd += COIN_SCORE;
						scoresEditor.putInt("score", coinScoreAdd);
						scoresEditor.commit();

						Log.i(TAG, "setting delete status as true");

						if (obA.get("ID").equals(heroID)
								&& obB.get("ID").equals(coinID)) {

							obB.put("deleteStatus", true);

						} else if (obB.get("ID").equals(heroID)
								&& obA.get("ID").equals(coinID)) {

							obA.put("deleteStatus", true);

						}

						Log.i(TAG, "checking if effectsOn is true");
						if (audioOptions.getBoolean("effectsOn", true)) {

							Level2Activity.mMunch.play();

						}

					}
				} catch (JSONException e) {
					// TODO: handle exception
					Log.e(TAG, "error at contact listener");
				}
			}
		});
		this.gameBackground.registerUpdateHandler(this
				.removeObjectUpdatehandler());
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	private IUpdateHandler removeObjectUpdatehandler() {

		return new IUpdateHandler() {

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUpdate(float pSecondsElapsed) {
				// TODO Auto-generated method stub

				JSONObject currentJsonObj;

				for (int i = 0; i < Level2Activity.this.deletejsonList.size(); i++) {

					currentJsonObj = Level2Activity.this.deletejsonList.get(i);

					try {

						if (currentJsonObj.getBoolean("deleteStatus")) {
							Log.i(TAG, "Entered method to remove coin");

							final AnimatedSprite Object = (AnimatedSprite) currentJsonObj
									.get("sprite");

							currentJsonObj.put("deleteStatus", false);

							final Scene scene = Level2Activity.this.mEngine
									.getScene();
							final Body body = (Body) currentJsonObj.get("body");
							level2PhysicsWorld.destroyBody(body);
							// scene.detachChild(Object);
							Object.setVisible(false);
						}
					} catch (JSONException e) {
						// TODO: handle exception
						Log.e(TAG,
								"exception inside update handler removeobjectupdate handler");
					}
				}
			}
		};
	}

	private Runnable mCoinSceneAdd = new Runnable() {
		public void run() {

			mEngine.setScene(scoreScene);
		}
	};

	private Runnable mPlayGameOverSound = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			mEngine.setScene(gameOverScene);
			Level2Activity.mGameOver.play();
		}
	};

	private Runnable mTry = new Runnable() {
		public void run() {

			// mEngine.getScene().back();
			Intent tryIntent = new Intent(Level2Activity.this, TryAgain.class);
			startActivity(tryIntent);
		}
	};

	private Runnable mEndGame = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Level2Activity.this.finish();
			Intent maintent = new Intent(Level2Activity.this,
					MenuActivity.class);
			startActivity(maintent);
		}
	};

	private void addCoin(final float pX, final float pY) {
		// TODO Auto-generated method stub
		final AnimatedSprite coin;
		final Body coinBody;
		coin = new AnimatedSprite(pX, pY, coinTextureRegion.deepCopy(),
				this.getVertexBufferObjectManager());
		coinBody = PhysicsFactory.createCircleBody(level2PhysicsWorld, coin,
				BodyType.StaticBody, PhysicsFactory.createFixtureDef(0, 0, 0));
		level2PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(coin,
				coinBody, true, false));
		this.gameBackground.getLastChild().attachChild(coin);
		coinObject = makeJsonData(coinID, coinBody, coin);
		deletejsonList.add(coinObject);
		coinBody.setUserData(coinObject);
		coin.animate(100);

	}

	private void addRoller(final float pX, final float pY) {
		// TODO Auto-generated method stub
		final AnimatedSprite roller;
		final Body body;
		final FixtureDef fixDef;
		roller = new AnimatedSprite(0, 0, rollerTextureRegion.deepCopy(),
				this.getVertexBufferObjectManager());
		roller.setPosition(pX, pY);
		fixDef = PhysicsFactory.createFixtureDef(0, 0, 0);
		body = PhysicsFactory.createBoxBody(level2PhysicsWorld, roller,
				BodyType.StaticBody, fixDef);
		rollerObject = makeJsonData(rollerID, body, roller);
		body.setUserData(rollerObject);
		roller.animate(100);
		level2PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				roller, body, true, false));
		this.gameBackground.getLastChild().attachChild(roller);

	}

	private IUpdateHandler setUpVelocity() {
		// TODO Auto-generated method stub
		return new IUpdateHandler() {

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUpdate(float pSecondsElapsed) {
				// TODO Auto-generated method stub
				if (direcUp == 2) {
					if (upknife.getY() <= endingUpY) {
						direcUp = 1;
						// upknife.setFlippedVertical(true);
						knifeUpbody.setLinearVelocity(0,
								knifeUpbody.getLinearVelocity().y * -1);
					}

				} else if (direcUp == 1) {
					if (upknife.getY() >= startingUpY) {

						direcUp = 2;
						// upknife.setFlippedVertical(true);
						knifeUpbody.setLinearVelocity(0,
								knifeUpbody.getLinearVelocity().y * -1);
					}

				}
			}
		};
	}

	private IUpdateHandler setDownVelocity() {
		// TODO Auto-generated method stub
		return new IUpdateHandler() {

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUpdate(float pSecondsElapsed) {
				// TODO Auto-generated method stub

				if (direcDown == 1) {
					if (knife.getY() >= endingY) {
						direcDown = 2;
						// knife.setFlippedVertical(true);
						knifeDownbody.setLinearVelocity(0,
								knifeDownbody.getLinearVelocity().y * -1);
					}
				} else if (direcDown == 2) {

					if (knife.getY() <= startingY) {
						direcDown = 1;
						// knife.setFlippedVertical(true);
						knifeDownbody.setLinearVelocity(0,
								knifeDownbody.getLinearVelocity().y * -1);
					}

				}
			}
		};
	}

	private JSONObject makeJsonData(String identify, Body body, Object sprite) {

		JSONObject myObject = new JSONObject();

		try {
			myObject.put("ID", identify);
			myObject.put("deleteStatus", false);
			myObject.put("body", body);
			myObject.put("sprite", sprite);

		} catch (JSONException e) {
			// TODO: handle exception
			Log.d(TAG, "Exception creating user data:" + e);
		}
		return myObject;
	}

	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub

		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(),
				pAccelerationData.getY());
		this.level2PhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
	}

	@Override
	public synchronized void onResumeGame() {
		// TODO Auto-generated method stub
		super.onResumeGame();
		this.enableAccelerationSensor(this);
	}

	@Override
	public synchronized void onPauseGame() {
		// TODO Auto-generated method stub
		super.onPauseGame();
		this.disableAccelerationSensor();
	}
	
	
}
