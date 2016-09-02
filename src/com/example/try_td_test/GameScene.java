package com.example.try_td_test;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Audio;
import android.provider.SyncStateContract.Helpers;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.try_gameengine.action.MAction;
import com.example.try_gameengine.action.MAction2;
import com.example.try_gameengine.action.MathUtil;
import com.example.try_gameengine.action.MovementAction;
import com.example.try_gameengine.action.MovementActionInfo;
import com.example.try_gameengine.action.MovementActionItemBaseReugularFPS;
import com.example.try_gameengine.action.MovementAtionController;
import com.example.try_gameengine.action.MAction.MActionBlock;
import com.example.try_gameengine.action.listener.IActionListener;
import com.example.try_gameengine.avg.GraphicsUtils;
import com.example.try_gameengine.framework.ALayer;
import com.example.try_gameengine.framework.ALayer.LayerParam;
import com.example.try_gameengine.framework.ButtonLayer;
import com.example.try_gameengine.framework.ButtonLayer.OnClickListener;
import com.example.try_gameengine.framework.GameModel;
import com.example.try_gameengine.framework.GameView;
import com.example.try_gameengine.framework.IGameController;
import com.example.try_gameengine.framework.IGameModel;
import com.example.try_gameengine.framework.ILayer;
import com.example.try_gameengine.framework.LabelLayer;
import com.example.try_gameengine.framework.Layer;
import com.example.try_gameengine.framework.LayerManager;
import com.example.try_gameengine.framework.LightImage;
import com.example.try_gameengine.framework.Sprite;
import com.example.try_gameengine.framework.StatusBar;
import com.example.try_gameengine.map.Field2D;
import com.example.try_gameengine.scene.EasyScene;
import com.example.try_gameengine.utils.DetectArea;
import com.example.try_gameengine.utils.DetectAreaRequest;
import com.example.try_gameengine.utils.DetectAreaSpriteRect;
import com.example.try_gameengine.utils.GameTimeUtil;
import com.example.try_gameengine.utils.IDetectAreaRequest;
import com.example.try_gameengine.utils.ISpriteDetectAreaListener;
import com.example.try_gameengine.utils.SpriteDetectAreaHandler;
import com.example.try_gameengine.utils.SpriteDetectAreaHelper;



public class GameScene extends EasyScene implements ButtonLayer.OnClickListener{
	private int selectTurret = -1;

	private Field2D field;

	private String[] turrets = new String[] { "assets/bulletTurret.png",
			"assets/bombTurret.png", "assets/poisonTurret.png",
			"assets/laserTurret.png", "assets/bullet.png" };

	class Actor extends Sprite{
		
		protected void addLayer(ALayer layer){
			
		};
	}
	/**
	 * 子弹用类
	 * 
	 */
	class Bullet extends Actor {

		private float dir;

		private int damage;

		private float x, y;

		private boolean removeFlag;
		
		private GameTimeUtil gameTimeUtil;
		
		private float speed = 10;

		ILayer targetLayer;
		float targetX; 
		float targetY;
		
		public Bullet(String fileName, float dir, int damage) {
			this.dir = dir;
			this.damage = damage;
			this.setBitmapAndAutoChangeWH(GraphicsUtils.loadImage(fileName));
//			this.setDelay(50);
			gameTimeUtil = new GameTimeUtil(50);
			
//			DetectArea a = SpriteDetectAreaHelper.createDetectAreaRect(this.getFrame());
			DetectArea a = new DetectAreaSpriteRect(new RectF(), new DetectAreaSpriteRect.SpriteRectListener() {
				
				@Override
				public RectF caculateSpriteRect() {
					// TODO Auto-generated method stub
					RectF rectF;
					if(getLocationInScene()!=null)
						rectF = new RectF(getLocationInScene().x, getLocationInScene().y, getLocationInScene().x + w, getLocationInScene().y + h);
					else
						rectF = getFrame();
					return rectF;
				}
				
				@Override
				public PointF caculateSpriteCenter() {
					// TODO Auto-generated method stub;
					PointF pointF;
					if(getLocationInScene()!=null)
						pointF = new PointF(getLocationInScene().x + w/2, getLocationInScene().y + h/2);
					else
						pointF = new PointF(getFrame().centerX(), getFrame().centerY());
					return pointF;
				}
			});
			
			SpriteDetectAreaHandler spriteDetectAreaHandler = new SpriteDetectAreaHandler();
//			spriteDetectAreaHandler.addSuccessorDetectAreaByRound(new PointF(getCenterX(), getCenterY()), this.range);
			spriteDetectAreaHandler.addSuccessorDetectArea(a, new ISpriteDetectAreaListener() {
				
				@Override
				public void didDetected(DetectArea handlerDetectArea,
						IDetectAreaRequest requestDetectArea) {
					// TODO Auto-generated method stub
//					Sheep sheep = (Sheep) handlerDetectArea.getObjectTag();
					PointF center = handlerDetectArea.getCenter();
					PointF requestCenter = requestDetectArea.getDetectArea().getCenter();
					// 当敌人存在

					
						Enemy e = (Enemy) requestDetectArea.getObjectTag();
						// 减少敌方HP
						e.hp -= Bullet.this.damage;
						e.hpBar.setUpdate(e.hp);
						removeFlag = true;
						// 从Layer中删除自身
//						getLayer().removeObject(this);
						removeFromParent();
				}

				@Override
				public boolean stopDoSuccessorDetected(
						DetectArea handlerDetectArea,
						IDetectAreaRequest requestDetectArea, boolean isDetected) {
					// TODO Auto-generated method stub

					return false;
				}
			});

			spriteDetectAreaHandler.apply();
			setSpriteDetectAreaHandler(spriteDetectAreaHandler);
		}

		@Override
		public void addChild(ILayer layer) {
			// TODO Auto-generated method stub
			super.addChild(layer);
			
			if(layer instanceof Actor)
				((Actor)layer).addLayer(this);
		}
		
		protected void addLayer(ALayer layer) {
			this.x = this.getX();
			this.y = this.getY();
		}

		public void action(long t) {
			if (removeFlag) {
				return;
			}
			
			if(!gameTimeUtil.isArriveExecuteTime())
				return;
			
//			Object o = null;
//			for (int i = 0; i < 6; i++) {
//				// 矫正弹道位置
//				double angle = Math.toRadians((double) this.dir);
//				this.x += Math.cos(angle);
//				this.y += Math.sin(angle);
//			}
//			this.setLocation((int) this.x
//					+ (field.getTileWidth() - this.getWidth()) / 2,
//					(int) this.y + (field.getTileHeight() - this.getHeight())
//							/ 2);
			
//			this.setPosition((int) this.x
//					+ (field.getTileWidth() - this.getWidth()) / 2,
//					(int) this.y + (field.getTileHeight() - this.getHeight())
//							/ 2);
			

			
			boolean didDealWillLayer = LayerManager.iterateAllLayers(new LayerManager.IterateLayersListener() {
				int x, y;
				
				@Override
				public boolean dealWithLayer(ILayer layer) {
					// TODO Auto-generated method stub
					if(targetLayer==null && layer instanceof Enemy)
						targetLayer = layer;
					
					if(targetLayer !=null){
//						float targetX = layer.getX(); 
//						float targetY = layer.getY(); 
						
						if(targetLayer.getLocationInScene()!=null){
							targetX = targetLayer.getLocationInScene().x + targetLayer.getWidth()/2; 
							targetY = targetLayer.getLocationInScene().y + targetLayer.getHeight()/2;
						}
						
						float startX = getLocationInScene().x + getWidth()/2; 
						float startY = getLocationInScene().y + getHeight()/2;
						
						final MathUtil mathUtil = new MathUtil();
						final float angle = mathUtil.getNewAngleTowardsPointF(targetX, targetY, startX, startY);
							
//						mathUtil.setXY(dx, dy);
						
//						mathUtil.setINITSPEEDX(mathUtil.genTotalSpeed());
						
						mathUtil.setINITSPEEDX(speed);
						
						float dx = mathUtil.getSpeedX(angle);
						
						float dy = mathUtil.getSpeedY(angle);
						
						move(dx, dy);
						
//						DetectArea detectArea = SpriteDetectAreaHelper.createDetectAreaPoint(new PointF(targetX, targetY));
						DetectArea detectArea = SpriteDetectAreaHelper.createDetectAreaRect(new RectF(targetX-20, targetY-20, targetX+20, targetY+20));
						IDetectAreaRequest request = new DetectAreaRequest(detectArea);
						request.setObjectTag(targetLayer);
						boolean isDetected = getSpriteDetectAreaHandler().detectByDetectAreaRequest(request);
//						if(!isDetected)
//							if (Bullet.this.getX() <= 12
//							|| Bullet.this.getX() >= Bullet.this.getParent().getParent().getWidth() - 12
//							|| Bullet.this.getY() <= 12
//							|| Bullet.this.getY() >= Bullet.this.getParent().getParent().getHeight() - 12) {
//									removeFlag = true;
//				//					this.getLayer().removeObject(this);
//									removeFromParent();
//							}
						return true;
					}
					
					return false;
				}
			});
			
			if(!didDealWillLayer){
				removeFlag = true;
				removeFromParent();
			}
//			o = this.getOnlyCollisionObject(Enemy.class);
//			// 当与敌相撞时
//			if (o != null) {
//				Enemy e = (Enemy) o;
//				// 减少敌方HP
//				e.hp -= this.damage;
//				e.hpBar.setUpdate(e.hp);
//				removeFlag = true;
//				// 从Layer中删除自身
////				getLayer().removeObject(this);
//				removeFromParent();
//				
//				return;
//				// 超出游戏画面时删除自身
//			} else if (this.getX() <= 12
//					|| this.getX() >= this.getParent().getWidth() - 12
//					|| this.getY() <= 12
//					|| this.getY() >= this.getParent().getHeight() - 12) {
//				removeFlag = true;
////				this.getLayer().removeObject(this);
//				removeFromParent();
//			}
		}
	}
	
	/**
	 * 炮塔用类
	 * 
	 */
	class Turret extends Actor {

		private int range = 500;

		private int delay = 10;

		boolean selected;
		
		private GameTimeUtil gameTimeUtil;

		public Turret(String fileName) {
			this.setBitmapAndAutoChangeWH(GraphicsUtils.loadImage(fileName));
//			setDelay(100);
			gameTimeUtil = new GameTimeUtil(100);
			setAlpha(0);
			
			DetectArea a = SpriteDetectAreaHelper.createDetectAreaRound(new PointF(getCenterX(), getCenterY()), this.range);
			SpriteDetectAreaHandler spriteDetectAreaHandler = new SpriteDetectAreaHandler();
//			spriteDetectAreaHandler.addSuccessorDetectAreaByRound(new PointF(getCenterX(), getCenterY()), this.range);
			spriteDetectAreaHandler.addSuccessorDetectArea(a, new ISpriteDetectAreaListener() {
				
				@Override
				public void didDetected(DetectArea handlerDetectArea,
						IDetectAreaRequest requestDetectArea) {
					// TODO Auto-generated method stub
//					Sheep sheep = (Sheep) handlerDetectArea.getObjectTag();
					PointF center = handlerDetectArea.getCenter();
					PointF requestCenter = requestDetectArea.getDetectArea().getCenter();
					// 当敌人存在

						Enemy target = (Enemy) requestDetectArea.getObjectTag();
						// 旋转炮台对准Enemy坐标
						setRotation((int) Math.toDegrees(Math.atan2(
								(target.getY() - Turret.this.getY()), (target.getX() - Turret.this
										.getX()))));


					// 延迟炮击
					if (Turret.this.delay > 0) {
						--Turret.this.delay;
					} else {
						// 构造炮弹
						Bullet bullet = new Bullet(turrets[4], Turret.this.getRotation(), 2);
						// 计算炮击点
						/*
						int x = (int) ((int) Math.round(Math.cos(Math.toRadians(Turret.this
								.getRotation()))
								* (double) bullet.getWidth() * 2)
								+ Turret.this.getX());

						int y = (int) ((int) Math.round(Math.sin(Math.toRadians(Turret.this
								.getRotation()))
								* (double) bullet.getHeight() * 2)
								+ Turret.this.getY());*/
//						bullet.setPosition(getX(), getY());
						// 注入炮弹到Layer
						Turret.this.addChild(bullet);
						Turret.this.delay = 10;
					}
				}

				@Override
				public boolean stopDoSuccessorDetected(
						DetectArea handlerDetectArea,
						IDetectAreaRequest requestDetectArea, boolean isDetected) {
					// TODO Auto-generated method stub
					return false;
				}
			});

			spriteDetectAreaHandler.apply();
			setSpriteDetectAreaHandler(spriteDetectAreaHandler);
		}
		
		@Override
		public void addChild(ILayer layer) {
			// TODO Auto-generated method stub
			super.addChild(layer);
			
			if(layer instanceof Actor)
				((Actor)layer).addLayer(this);
		}

		public void addLayer(ALayer layer) {
//			// 让角色渐进式出现
//			FadeTo fade = fadeOut();
//			// 监听渐进淡出事件
//			fade.setActionListener(new ActionListener() {
//
//				public void process(Actor o) {
//
//				}
//
//				public void start(Actor o) {
//
//				}
//
//				// 当渐进完毕时
//				public void stop(Actor o) {
//					// 旋转90度
//					rotateTo(90);
//				}
//
//			});

			MovementAction action = MAction.alphaAction(2000, 0, 255);
			action.setActionListener(new IActionListener() {
				
				@Override
				public void beforeChangeFrame(int nextFrameId) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void afterChangeFrame(int periousFrameId) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void actionStart() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void actionFinish() {
					// TODO Auto-generated method stub
					runMovementAction(MAction.rotationToAction(2000, 90));
				}
				
				@Override
				public void actionCycleFinish() {
					// TODO Auto-generated method stub
					
				}
			});
			runMovementAction(action);
		}

		@Override
		public void drawSelf(Canvas g, Paint paint) {
			super.drawSelf(g, paint);
			
			if (selected) {
				getPaint().setColor(Color.rgb(0, 0, 100));				
				g.drawOval(new RectF(-(range * 2 - field.getTileWidth()) / 2,
						-(range * 2 - field.getTileHeight()) / 2,
						this.range * 2 - 1, this.range * 2 - 1), getPaint());
				
				getPaint().setColor(Color.RED);
				float left = -(range * 2 - field.getTileWidth()) / 2;
				float top = -(range * 2 - field.getTileHeight()) / 2;
				g.drawOval(new RectF(left, top,
						left + this.range * 2 - 1, top + this.range * 2 - 1), getPaint());
			}
		}

		public void action(long t) {
			for(ILayer layer : getLayers()){
				if(layer instanceof Bullet )
					((Bullet)layer).action(((GameModel)gameModel).getInterval());
			}
			
			if(!gameTimeUtil.isArriveExecuteTime())
				return;
			
			LayerManager.iterateAllLayers(new LayerManager.IterateLayersListener() {
				
				@Override
				public boolean dealWithLayer(ILayer layer) {
					// TODO Auto-generated method stub
					if(layer instanceof Enemy){
						DetectArea detectArea = SpriteDetectAreaHelper.createDetectAreaPoint(new PointF(((Enemy)layer).getCenterX(), ((Enemy)layer).getCenterY()));
						IDetectAreaRequest request = new DetectAreaRequest(detectArea);
						request.setObjectTag(layer);
						getSpriteDetectAreaHandler().detectByDetectAreaRequest(request);
						return true;
					}
					
					return false;
				}
			});

		}
		
		@Override
		public void setSpriteDetectAreaHandler(
				SpriteDetectAreaHandler spriteDetectAreaHandler) {
			// TODO Auto-generated method stub
			super.setSpriteDetectAreaHandler(spriteDetectAreaHandler);
		}
		
		@Override
		protected void onTouched(MotionEvent event) {
			// TODO Auto-generated method stub
			super.onTouched(event);
			
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				this.selected = true;
			}
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			// TODO Auto-generated method stub
			
			if(event.getAction()==MotionEvent.ACTION_UP||event.getAction()==MotionEvent.ACTION_CANCEL){
				this.selected = false;
			}
			
			return super.onTouchEvent(event);
		}
	}

	/**
	 * 敌兵用类
	 * 
	 */
	class Enemy extends Actor {

		private int startX, startY;

		private int endX, endY;

		private int speed, hp;

		private boolean removeFlag;

		// 使用精灵StatusBar充当血槽
		protected StatusBar hpBar;
		
		private GameTimeUtil gameTimeUtil;
		
		public Enemy(String fileName, int sx, int sy, int ex, int ey,
				int speed, int hp) {
//			this.setDelay(300);
			gameTimeUtil = new GameTimeUtil(300);
			
			this.setBitmapAndAutoChangeWH(GraphicsUtils.loadImage(fileName));
			this.hpBar = new StatusBar(hp, hp, (this.getWidth() - 25) / 2, this
					.getHeight() + 5, 25, 5);
			this.startX = sx;
			this.startY = sy;
			this.endX = ex;
			this.endY = ey;
			this.speed = speed;
			this.hp = hp;
			
			addChild(hpBar);
		}

		@Override
		public void drawSelf(Canvas canvas, Paint paint) {
			// TODO Auto-generated method stub
			super.drawSelf(canvas, paint);
			
			draw(canvas);
		}
		
		public void draw(Canvas g) {
			// 绘制精灵
//			hpBar.createUI(g);
			hpBar.drawSelf(g, null);
			
			if (hp <= 0 && !removeFlag) {
//				// 设定死亡时渐变
//				FadeTo fade = fadeIn();
//				// 渐变时间为30毫秒
//				fade.setSpeed(30);
//				// 监听渐变过程
//				fade.setActionListener(new ActionListener() {
//
//					public void process(Sprite o) {
//
//					}
//
//					public void start(Sprite o) {
//
//					}
//
//					public void stop(Sprite o) {
//						Enemy.this.removeActionEvents();
//						Enemy.this.getLayer().removeObject(Enemy.this);
//					}
//
//				});
				
				MovementAction action = MAction.alphaAction(1000, 255, 0);
				action.setActionListener(new IActionListener() {
					
					@Override
					public void beforeChangeFrame(int nextFrameId) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void afterChangeFrame(int periousFrameId) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void actionStart() {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void actionFinish() {
						// TODO Auto-generated method stub
						removeFromParent();
					}
					
					@Override
					public void actionCycleFinish() {
						// TODO Auto-generated method stub
						
					}
				});
				runMovementAction(action);

				this.removeFlag = true;
			}
		}

		public void action(long t) {
			if(!gameTimeUtil.isArriveExecuteTime())
				return;
//			// 触发精灵事件
//			hpBar.update(t);
//			if (hp <= 0 && !removeFlag) {
//				// 设定死亡时渐变
//				FadeTo fade = fadeIn();
//				// 渐变时间为30毫秒
//				fade.setSpeed(30);
//				// 监听渐变过程
//				fade.setActionListener(new ActionListener() {
//
//					public void process(Sprite o) {
//
//					}
//
//					public void start(Sprite o) {
//
//					}
//
//					public void stop(Sprite o) {
//						Enemy.this.removeActionEvents();
//						Enemy.this.getLayer().removeObject(Enemy.this);
//					}
//
//				});
//
//				this.removeFlag = true;
//			}
		}
		
		@Override
		public void addChild(ILayer layer) {
			// TODO Auto-generated method stub
			super.addChild(layer);
			
			if(layer instanceof Actor)
				((Actor)layer).addLayer(this);
		}

		@Override
		// 首次注入Layer时调用此函数
		protected void addLayer(ALayer layer) {

			// 坐标矫正，用以让角色居于瓦片中心
			final int offsetX = (field.getTileWidth() - this
					.getWidth()) / 2;
			final int offsetY = (field.getTileWidth() - this
					.getHeight()) / 2;
			// 初始化角色在Layer中坐标
			setPosition(startX + offsetX, startY + offsetY);
			
//			MovementAction action = MAction.moveTo(endX, endY, 2000);
			final MoveTo move = new MoveTo(field, endX, endY, false);
			
//			MovementAction moveAction = MAction.moveTo(0, 0, 2000);
			MovementAction moveAction = MAction.runBlockNoDelay(new MActionBlock() {
				
				@Override
				public void runBlock() {
					// TODO Auto-generated method stub
					if(move.isComplete())
						removeFromParent();
					else{
						move.update(0);
						// 矫正坐标，让角色居中
						setPosition(getX() + offsetX, getY() + offsetY);
						// 获得角色移动方向
						switch (move.getDirection()) {
						case Field2D.TUP:
							// 根据当前移动方向，变更角色旋转方向（以下同）
							setRotation(270);
							break;
						case Field2D.TLEFT:
							setRotation(180);
							break;
						case Field2D.TRIGHT:
							setRotation(0);
							break;
						case Field2D.TDOWN:
							setRotation(90);
							break;
						default:
							break;
						}
					}
				}
			});
			
			MovementAction action = MAction.repeatForever(moveAction);
			
			moveAction.setActionListener(new IActionListener() {
				
				@Override
				public void beforeChangeFrame(int nextFrameId) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void afterChangeFrame(int periousFrameId) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void actionStart() {
					// TODO Auto-generated method stub
					move.start(Enemy.this);
					move.setSpeed(speed);
					move.onLoad();
					
				}
				
				@Override
				public void actionFinish() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void actionCycleFinish() {
					// TODO Auto-generated method stub
					
				}
			});
			
			runMovementAction(action);
			
//			// 命令角色向指定坐标自行移动(参数为false为四方向寻径，为true时八方向)，并返回移动控制器
//			// PS:endX与endY非显示位置，所以不必矫正
//			final MoveTo move = moveTo(endX, endY, false);
//			// 启动角色事件监听
//			move.setActionListener(new ActionListener() {
//				// 截取事件进行中数据
//				public void process(Actor o) {
//					// 矫正坐标，让角色居中
//					o.setLocation(o.getX() + offsetX, o.getY() + offsetY);
//					// 获得角色移动方向
//					switch (move.getDirection()) {
//					case Field2D.TUP:
//						// 根据当前移动方向，变更角色旋转方向（以下同）
//						o.setRotation(270);
//						break;
//					case Field2D.TLEFT:
//						o.setRotation(180);
//						break;
//					case Field2D.TRIGHT:
//						o.setRotation(0);
//						break;
//					case Field2D.TDOWN:
//						o.setRotation(90);
//						break;
//					default:
//						break;
//					}
//
//				}
//
//				public void start(Sprite o) {
//
//				}
//
//				// 当角色移动完毕时
//				public void stop(Sprite o) {
//					// 从Layer中删除此角色
////					layer.removeObject(o);
//				}
//
//			});
//			// 设定移动速度
//			move.setSpeed(speed);
		}
	}

	// 起始点
	class Begin extends Sprite {
		public Begin(String fileName) {
			setBitmap(GraphicsUtils.loadImage(fileName));
		}
	}

	// 结束点
	class End extends Sprite {
		public End(String fileName) {
			setBitmap(GraphicsUtils.loadImage(fileName));
		}
	}

	/**
	 * 拖拽用菜单
	 * 
	 */
	class Menu extends Layer {
		public Menu() {
			super(128, 240, false);

			// 设定menu层级高于MapLayer
//			setLayer(101);
			setzPosition(101);
			// 不锁定menu移动
//			setLocked(false);
//			setLimitMove(false);
			// 锁定Actor拖拽
//			setActorDrag(false);
//			setDelay(500);
			// 设定Menu背景
//			LImage image = LImage.createImage(this.getWidth(),
//					this.getHeight(), true);
//			LGraphics g = image.getLGraphics();
			Bitmap bitmap = Bitmap.createBitmap(this.getWidth(),
					this.getHeight(), Config.ARGB_8888);
			Canvas g = new Canvas(bitmap);
			Paint paint = new Paint();
			paint.setColor(Color.rgb(0,0,0));
			paint.setAlpha(125);
			paint.setStyle(Style.FILL);
			g.drawRect(0, 0, getWidth(), getHeight(),paint);
			paint.setColor(Color.WHITE);
			paint.setTextSize(15);
			g.drawText("我是可拖拽菜单", 12, 25, paint);
//			g.dispose();
			
			setBitmap(bitmap);

			Sprite bulletTurret = new Sprite() {
				// 当选中当前按钮时，为按钮绘制选中框(以下同)
				@Override
				public void drawSelf(Canvas canvas, Paint paint) {
					// TODO Auto-generated method stub
					super.drawSelf(canvas, paint);
					
					if (selectTurret == 0) {
						
						Paint paintDrawRect = new Paint();
						paintDrawRect.setColor(Color.RED);
						canvas.drawRect(2, 2, this.getWidth() - 4,
								this.getHeight() - 4, paintDrawRect);
					}
				}
			};
			bulletTurret.setBitmapAndAutoChangeWH(GraphicsUtils.loadImage(turrets[0]));
			bulletTurret.setPosition(18, 64);
			
			Sprite bombTurret = new Sprite() {

				@Override
				public void drawSelf(Canvas canvas, Paint paint) {
					// TODO Auto-generated method stub
					super.drawSelf(canvas, paint);
					
					if (selectTurret == 1) {
						
						Paint paintDrawRect = new Paint();
						paintDrawRect.setColor(Color.RED);
						canvas.drawRect(2, 2, this.getWidth() - 4,
								this.getHeight() - 4, paintDrawRect);
					}
				}
			};
			bombTurret.setOnLayerClickListener(new OnLayerClickListener() {
				
				@Override
				public void onClick(ILayer layer) {
					// TODO Auto-generated method stub
					selectTurret = 1;
				}
			});
			bombTurret.setBitmapAndAutoChangeWH(GraphicsUtils.loadImage(turrets[1]));
			bombTurret.setPosition(78, 64);
			
			Sprite poisonTurret = new Sprite() {

				@Override
				public void drawSelf(Canvas canvas, Paint paint) {
					// TODO Auto-generated method stub
					super.drawSelf(canvas, paint);
					
					if (selectTurret == 2) {
						
						Paint paintDrawRect = new Paint();
						paintDrawRect.setColor(Color.RED);
						canvas.drawRect(2, 2, this.getWidth() - 4,
								this.getHeight() - 4, paintDrawRect);
//						canvas.resetColor();
//						getPaint().reset();
					}
				}
			};
			
			poisonTurret.setOnLayerClickListener(new OnLayerClickListener() {
				
				@Override
				public void onClick(ILayer layer) {
					// TODO Auto-generated method stub
					selectTurret = 2;
				}
			});

			poisonTurret.setBitmapAndAutoChangeWH(GraphicsUtils.loadImage(turrets[2]));
			
			poisonTurret.setPosition(18, 134);
			
			Sprite laserTurret = new Sprite(){
				@Override
				public void drawSelf(Canvas canvas, Paint paint) {
					// TODO Auto-generated method stub
					super.drawSelf(canvas, paint);
					
					if (selectTurret == 3) {					
						Paint paintDrawRect = new Paint();
						paintDrawRect.setColor(Color.RED);
						canvas.drawRect(2, 2, this.getWidth() - 4,
								this.getHeight() - 4, paintDrawRect);
//						canvas.resetColor();
//						getPaint().reset();
					}
				}
			};
			
			laserTurret.setOnLayerClickListener(new OnLayerClickListener() {
				
				@Override
				public void onClick(ILayer layer) {
					// TODO Auto-generated method stub
					selectTurret = 3;
				}
			});
			
			laserTurret.setBitmapAndAutoChangeWH(GraphicsUtils.loadImage(turrets[3]));
//			laserTurret.setLocation(78, 134);
			laserTurret.setPosition(78, 134);
			
			// 用LPaper制作敌人增加按钮
			Sprite button = new Sprite(0, 0, false);
			button.setBitmapAndAutoChangeWH(GraphicsUtils.loadImage("assets/button.png"));
			button.setOnLayerClickListener(new OnLayerClickListener() {
				
				@Override
				public void onClick(ILayer layer) {
					// TODO Auto-generated method stub
					// 获得MapLayer
					MapLayer mapLayer = GameScene.this.layer;
					// 开始游戏演算
					mapLayer.doStart();
				}
			});
//			{
//				public void downClick() {
//					// 获得MapLayer
//					MapLayer layer = (MapLayer) getBottomLayer();
//					// 开始游戏演算
//					layer.doStart();
//				}		
//			};
				
			button.setPosition(27, 196);

			// 复合LPaper到Layer
			addChild(bulletTurret);
			addChild(bombTurret);
			addChild(poisonTurret);
			addChild(laserTurret);
			addChild(button);
		}

		public void downClick(int x, int y) {
			selectTurret = -1;
		}

		@Override
		protected void onTouched(MotionEvent event) {
			// TODO Auto-generated method stub
			super.onTouched(event);
			
			if(event.getAction()==MotionEvent.ACTION_DOWN)
				downClick((int)event.getX(), (int)event.getY());
		}
	}
	
	/**
	 * 大地图用Layer
	 */
	class MapLayer extends Layer {
		private Field2D tmpField;
		
		GameTimeUtil gameTimeUtil;
		
		private boolean start;

		private int startX, startY, endX, endY;

		private int index, count;
		
		HashMap pathMap = new HashMap(10);
		
		@Override
		public void addChild(ILayer layer) {
			// TODO Auto-generated method stub
			super.addChild(layer);
			
			if(layer instanceof Actor)
				((Actor)layer).addLayer(this);
		}
		
		public void setField2D(Field2D field) {
			if (field == null) {
				return;
			}
			if (tmpField != null) {
				if ((field.getMap().length == tmpField.getMap().length)
						&& (field.getTileWidth() == tmpField.getTileWidth())
						&& (field.getTileHeight() == tmpField.getTileHeight())) {
					tmpField.set(field.getMap(), field.getTileWidth(), field
							.getTileHeight());
				}
			} else {
				tmpField = field;
			}
		}
		
		public void setField2DBackground(Field2D field, HashMap<?, ?> pathMap) {
			setField2DBackground(field, pathMap, null);
		}

		public void setField2DBackground(Field2D field, HashMap<?, ?> pathMap,
				String fileName) {
			setField2D(field);
			Bitmap background = null;

//			if (fileName != null) {
////				LightImage tmp = LImage.createImage(fileName);
//				Bitmap bitmap = Bitmap.createBitmap(this.getWidth(),
//						this.getHeight(), Config.ARGB_8888);
//				background = setTileBackground(bitmap, true);
//				if (tmp != null) {
//					tmp.dispose();
//					tmp = null;
//				}
//			} else {
				Bitmap bitmap = Bitmap.createBitmap(this.getWidth(),
						this.getHeight(), Config.ARGB_8888);
				background = bitmap;
//			}

				setBitmap(background);
		}
		
		@Override
		public void drawSelf(Canvas canvas, Paint paint) {
			// TODO Auto-generated method stub
//			LGraphics g = background.getLGraphics();
			for (int i = 0; i < field.getWidth(); i++) {
				for (int j = 0; j < field.getHeight(); j++) {
					int index = field.getType(j, i);
					Object o = pathMap.get(index);
					if (o != null) {
						if (o instanceof Bitmap) {
							canvas.drawBitmap(((Bitmap) o), field.tilesToWidthPixels(i),
									field.tilesToHeightPixels(j), paint);
						} 
						
//						else if (o instanceof Actor) {
//							addObject(((Actor) o), field.tilesToWidthPixels(i),
//									field.tilesToHeightPixels(j));
//						}
					}
				}
			}
//			g.dispose();
//			background.setFormat(Format.SPEED);
//			setBitmap(background);
			
			super.drawSelf(canvas, paint);
			

		}

		@SuppressWarnings("unchecked")
		public MapLayer() {
			super(576, 480, true);
			// 不锁定MapLayer拖拽
//			setLocked(false);
			// 锁定MapLayer中角色拖拽
//			setActorDrag(false);
			// 设置MapLayer背景元素(键值需要与map.txt文件中标识相对应)
			
			pathMap.put(new Integer(0), GraphicsUtils.loadImage("assets/sand.png"));
			pathMap.put(new Integer(1), GraphicsUtils.loadImage("assets/sandTurn1.png"));
			pathMap.put(new Integer(2), GraphicsUtils.loadImage("assets/sandTurn2.png"));
			pathMap.put(new Integer(3),GraphicsUtils.loadImage("assets/sandTurn3.png"));
			pathMap.put(new Integer(4), GraphicsUtils.loadImage("assets/sandTurn4.png"));
			pathMap.put(new Integer(5), GraphicsUtils.loadImage("assets/base.png"));
			pathMap.put(new Integer(6), GraphicsUtils.loadImage("assets/castle.png"));

			// 为Layer加入简单的2D地图背景，瓦片大小32x32，以rock图片铺底
			setField2DBackground(new Field2D("assets/map.txt", 32, 32),
					pathMap, "assets/rock.png");

			GameScene.this.field = tmpField;
			// 敌人出现坐标
			this.startX = 64;
			this.startY = 416;
			// 敌人消失坐标
			this.endX = 480;
			this.endY = 416;

			// 设定MapLayer每隔2秒执行一次内部Action
//			setDelay(LSystem.SECOND * 2);
			
			gameTimeUtil = new GameTimeUtil(2000);
		}
		
		@Override
		public void frameTrig() {
			// TODO Auto-generated method stub
			super.frameTrig();
		}

		public void action(long t) {
			
			for(ILayer layer : getLayers()){
				if(layer instanceof Bullet )
					((Bullet)layer).action(((GameModel)gameModel).getInterval());
				else if(layer instanceof Turret)
					((Turret)layer).action(((GameModel)gameModel).getInterval());
			}
			
			if(!gameTimeUtil.isArriveExecuteTime())
				return;
			
			// 当启动标识为true时执行以下操作
			if (start) {
				if (index < 3) {
					Enemy enemy = null;
					// 根据点击next(增加敌人)的次数变换敌人样式
					switch (count) {
					case 0:
						enemy = new Enemy("assets/enemy.png", startX, startY,
								endX, endY, 2, 4);
						break;
					case 1:
						enemy = new Enemy("assets/fastEnemy.png", startX,
								startY, endX, endY, 4, 6);
						break;
					case 2:
						enemy = new Enemy("assets/smallEnemy.png", startX,
								startY, endX, endY, 3, 10);
						break;
					case 3:
						enemy = new Enemy("assets/bigEnemy.png", startX,
								startY, endX, endY, 1, 16);
						break;
					default:
						count = 0;
						enemy = new Enemy("assets/enemy.png", startX, startY,
								endX, endY, 2, 2);
						break;
					}
//					addObject(enemy);
					addChild(enemy);
					index++;
					// 否则复位
				} else {
					start = false;
					index = 0;
					count++;
				}
			}
		}

		private ILayer o = null;
		
		@Override
		protected void onTouched(MotionEvent event) {
			// TODO Auto-generated method stub
			super.onTouched(event);
			// TODO Auto-generated method stub
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				int newX = (int) (event.getX() / field.getTileWidth());
				int newY = (int) (event.getY() / field.getTileHeight());
				// 当选中炮塔(参数不为-1)且数组地图参数为-1(不可通过)并且无其它角色在此时
//				if (getLayers().size() == 0 && selectTurret != -1
//						&& field.getType(newY, newX) == -1) {
				if (selectTurret != -1
						&& field.getType(newY, newX) == -1) {
					// 添加炮塔
//					addObject(new Turret(turrets[selectTurret]), newX
//							* field.getTileWidth(), newY * field.getTileHeight());
					Turret turret = new Turret(turrets[selectTurret]);
					turret.setPosition(newX * field.getTileWidth(), newY * field.getTileHeight());
					addChild(turret);
				}
				
//				if(getLayers().size() != 0){
//					o = getLayers().peek();
//					if (o != null && o instanceof Turret) {
//						((Turret) o).selected = true;
//					}
//				}
			}else if(event.getAction()==MotionEvent.ACTION_UP){
//				if (o != null && o instanceof Turret) {
//					((Turret) o).selected = false;
//				}
			}
			
//			return super.onTouchEvent(event);
		}

		public void doStart() {
			this.start = true;
		}

	}
	
    LabelLayer scoreLab = new LabelLayer(0, 0, false); 
    LabelLayer appLab  = new LabelLayer(0, 0, false); 
    LabelLayer myLabel  = new LabelLayer(0, 0, false); 
    int appleNum = 0;
    
    float moveSpeed = 15.0f;
    float maxSpeed = 50.0f;
    float distance = 0.0f;
    float lastDis = 0.0f;
    float theY = 0.0f;
    boolean isLose = false;
	boolean isReadyToJump = false;

	MapLayer layer;
	Menu menu;
	
	public GameScene(Context context, String id, int level) {
		super(context, id, level);
		// TODO Auto-generated constructor stub
//		this.addAutoDraw(bg);
		
        int skyColor = Color.argb(255, 113, 197, 207);
        this.setBackgroundColor(skyColor);
        scoreLab.getPaint().setTextAlign(Align.LEFT);
        scoreLab.setPosition(20, 150);
        scoreLab.setText("run: 0 km");
        this.addAutoDraw(scoreLab);
        
        appLab.getPaint().setTextAlign(Align.LEFT);
        appLab.setPosition(400, 150);
        appLab.setText("eat: apple");
        this.addAutoDraw(appLab);
        
        myLabel.setText("");
        myLabel.setTextSize(100);
        myLabel.setzPosition(100);
        myLabel.setAutoHWByText();
        LayerParam layerParam = new LayerParam();
        layerParam.setPercentageX(0.5f);
        layerParam.setEnabledPercentagePositionX(true);
        myLabel.setLayerParam(new LayerParam());
        myLabel.setPosition(CommonUtil.screenWidth/2, CommonUtil.screenHeight/2);
        myLabel.setAnchorPoint(0.5f, 0);
        this.addAutoDraw(myLabel);
        
        isEnableRemoteController(false);
        
     // 构建地图用Layer
     		layer = new MapLayer();
     		// 居中
//     		centerOn(layer);
     		// 添加MapLayer到Screen
     		addAutoDraw(layer);
     		// 构建菜单用Layer
     		menu = new Menu();
     		// 让menu居于屏幕右侧
//     		rightOn(menu);
     		menu.setY(0);
     		// 添加menu到Screen
     		addAutoDraw(menu);
        
	}
	GameView gameView;
	
	
	void checkCollistion(){
	
	}

public void downAndUp(final Sprite sprite,float down, float downTime, float up, float upTime, boolean isRepeat){
    MovementAction downAct = MAction.moveByY(down, (long)(downTime*1000));
//    downAct.setTimerOnTickListener(new MovementAction.TimerOnTickListener() {
//		
//		@Override
//		public void onTick(float dx, float dy) {
//			// TODO Auto-generated method stub
//			sprite.move(dx, dy);
//		}
//	});
    //moveByX(CGFloat(0), y: down, duration: downTime)
    MovementAction upAct = MAction.moveByY(up, (long)(upTime*1000));
//    upAct.setTimerOnTickListener(new MovementAction.TimerOnTickListener() {
//		
//		@Override
//		public void onTick(float dx, float dy) {
//			// TODO Auto-generated method stub
//			sprite.move(dx, dy);
//		}
//	});
    
    //MAction use threadPool it would delay during action by action.
    MovementAction downUpAct = MAction2.sequence(new MovementAction[]{downAct,upAct});
    downUpAct.setMovementActionController(new MovementAtionController());
    if (isRepeat) {
    	sprite.runMovementActionAndAppend(MAction.repeatForever(downUpAct));
    }else {
    	sprite.runMovementActionAndAppend(downUpAct);
    }
    
    
}

	@Override
	public void initGameView(Activity activity, IGameController gameController,
			IGameModel gameModel) {
		// TODO Auto-generated method stub
		gameView = new GameView(activity, gameController, gameModel);
	}

	public void action(){
//		gameDog.alone();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction()==MotionEvent.ACTION_DOWN){
			if (isLose) {
	            reSet();
	        }else{
//	            if (panda.status != Status.jump2) {
//	            	AudioUtil.playJump();
//	            }
	            isReadyToJump = true;
	        }
		}
		LayerManager.onTouchLayers(event);
		return true;
	}
	
	private void checkGameOver(){
//	    if (panda.getX() + panda.w < 0 || panda.getY() > CommonUtil.screenHeight) {
//		    System.out.println("game over");
//		    myLabel.setText("game over");
//		    AudioUtil.playDead();
//		    isLose = true;
//		    AudioUtil.stopBackgroundMusic();
//	    }
	}
	
	public void reSet(){
        isLose = false;
//        panda.setPosition(200, 400);
//        panda.reset();
//        myLabel.setText("");
//        moveSpeed  = 15.0f;
//        distance = 0.0f;
//        lastDis = 0.0f;
//        appleNum = 0;
//        platformFactory.reset();
//        appleFactory.reSet();
//        platformFactory.createPlatform(3, 0, 400);
//        AudioUtil.playBackgroundMusic();
    }
	
	@Override
	public void process() {
		// TODO Auto-generated method stub
		if (isLose) {
			//do nothing
        }else{
        	LayerManager.processLayers();
        	
           
            distance += moveSpeed;
            lastDis -= moveSpeed;
            float tempSpeed = 5+(int)(distance/2000);
            if (tempSpeed > maxSpeed) {
                tempSpeed = maxSpeed;
            }
            if (moveSpeed < tempSpeed) {
                moveSpeed = tempSpeed;
            }
            
            if (lastDis < 0) {
//                platformFactory.createPlatformRandom();
            }
            distance += moveSpeed;
            int runKM = (int)((distance/1000*10)/10);
            scoreLab.setText("run: " + runKM + "km");
            appLab.setText("eat: " + appleNum + "apple");
//            platformFactory.move(moveSpeed, panda);
//            bg.move(moveSpeed/5);
//            appleFactory.move(moveSpeed);
//            appleFactory.process();
            
            checkCollistion();
            
            checkGameOver();
            
            layer.action(((GameModel)gameModel).getInterval());
//            menu.action(((GameModel)gameModel).getInterval());
        }
	}
	
//	@Override
//	public void onGetData(float dist, float theY) {
//		// TODO Auto-generated method stub
//		this.lastDis = dist;
//		this.theY = theY;
//        appleFactory.theY = theY;
//	}

	@Override
	public void doDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		LayerManager.drawLayers(canvas, null);
		
//		fight.drawSelf(canvas, null);
	}

	@Override
	public void beforeGameStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void arrangeView(Activity activity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setActivityContentView(Activity activity) {
		// TODO Auto-generated method stub
		activity.setContentView(gameView);
	}

	@Override
	public void afterGameStart() {
		// TODO Auto-generated method stub
		Log.e("game scene", "game start");
//		AudioUtil.playBackgroundMusic();
	}
	
	@Override
	protected void beforeGameStop() {
		// TODO Auto-generated method stub
		Log.e("game scene", "game stop");
//		AudioUtil.stopBackgroundMusic();
	}
	
	@Override
	protected void afterGameStop() {
		// TODO Auto-generated method stub
//		AudioUtil.stopBackgroundMusic();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onClick(ButtonLayer buttonLayer) {
		// TODO Auto-generated method stub

	}


}
