/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dmbteam.scheduler.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The listener interface for receiving swipeDismissRecyclerViewTouch events.
 * The class that is interested in processing a swipeDismissRecyclerViewTouch
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addSwipeDismissRecyclerViewTouchListener<code> method. When
 * the swipeDismissRecyclerViewTouch event occurs, that object's appropriate
 * method is invoked.
 *
 */
public class SwipeDismissRecyclerViewTouchListener implements
		View.OnTouchListener {
	// Cached ViewConfiguration and system-wide constant values
	/** The Slop. */
	private int mSlop;
	
	/** The Min fling velocity. */
	private int mMinFlingVelocity;
	
	/** The Max fling velocity. */
	private int mMaxFlingVelocity;
	
	/** The Animation time. */
	private long mAnimationTime;

	// Fixed properties
	/** The Recycler view. */
	private RecyclerView mRecyclerView;
	
	/** The Callbacks. */
	private DismissCallbacks mCallbacks;
	
	/** The View width. */
	private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero

	// Transient properties
	/** The Pending dismisses. */
	private List<PendingDismissData> mPendingDismisses = new ArrayList<PendingDismissData>();
	
	/** The Animated views. */
	private List<View> mAnimatedViews = new LinkedList<View>();
	
	/** The Dismiss animation ref count. */
	private int mDismissAnimationRefCount = 0;
	
	/** The Down x. */
	private float mDownX;
	
	/** The Down y. */
	private float mDownY;
	
	/** The Swiping. */
	private boolean mSwiping;
	
	/** The Swiping slop. */
	private int mSwipingSlop;
	
	/** The Velocity tracker. */
	private VelocityTracker mVelocityTracker;
	
	/** The Down position. */
	private int mDownPosition;
	
	/** The Down view. */
	private View mDownView;
	
	/** The Paused. */
	private boolean mPaused;

	/** The Animation lock. */
	private final Object mAnimationLock = new Object();

	/**
	 * The Interface DismissCallbacks.
	 */
	public interface DismissCallbacks {
		
		/**
		 * Can dismiss.
		 *
		 * @param position the position
		 * @return true, if successful
		 */
		boolean canDismiss(int position);

		/**
		 * On dismiss.
		 *
		 * @param recyclerView the recycler view
		 * @param reverseSortedPositions the reverse sorted positions
		 */
		void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions);
	}

	/**
	 * Instantiates a new swipe dismiss recycler view touch listener.
	 *
	 * @param recyclerView the recycler view
	 * @param callbacks the callbacks
	 */
	public SwipeDismissRecyclerViewTouchListener(RecyclerView recyclerView,
			DismissCallbacks callbacks) {
		ViewConfiguration vc = ViewConfiguration.get(recyclerView.getContext());
		mSlop = vc.getScaledTouchSlop();
		mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
		mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
		mAnimationTime = recyclerView.getContext().getResources()
				.getInteger(android.R.integer.config_shortAnimTime);
		mRecyclerView = recyclerView;
		mCallbacks = callbacks;
	}

	/**
	 * Sets the enabled.
	 *
	 * @param enabled the new enabled
	 */
	public void setEnabled(boolean enabled) {
		mPaused = !enabled;
	}

	/**
	 * Make scroll listener.
	 *
	 * @return the recycler view. on scroll listener
	 */
	public RecyclerView.OnScrollListener makeScrollListener() {
		return new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView,
					int newState) {
				setEnabled(newState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			}
		};
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (mViewWidth < 2) {
			mViewWidth = mRecyclerView.getWidth();
		}

		switch (motionEvent.getActionMasked()) {
		case MotionEvent.ACTION_DOWN: {
			if (mPaused) {
				return false;
			}

			// TODO: ensure this is a finger, and set a flag

			// Find the child view that was touched (perform a hit test)
			Rect rect = new Rect();
			int childCount = mRecyclerView.getChildCount();
			int[] listViewCoords = new int[2];
			mRecyclerView.getLocationOnScreen(listViewCoords);
			int x = (int) motionEvent.getRawX() - listViewCoords[0];
			int y = (int) motionEvent.getRawY() - listViewCoords[1];
			View child;
			for (int i = 0; i < childCount; i++) {
				child = mRecyclerView.getChildAt(i);
				child.getHitRect(rect);
				if (rect.contains(x, y)) {
					mDownView = child;
					break;
				}
			}

			if (mDownView != null) {
				mDownX = motionEvent.getRawX();
				mDownY = motionEvent.getRawY();
				mDownPosition = mRecyclerView.getChildPosition(mDownView);
				if (mCallbacks.canDismiss(mDownPosition)) {
					mVelocityTracker = VelocityTracker.obtain();
					mVelocityTracker.addMovement(motionEvent);
				} else {
					mDownView = null;
				}
			}
			return false;
		}

		case MotionEvent.ACTION_CANCEL: {
			if (mVelocityTracker == null) {
				break;
			}

			if (mDownView != null && mSwiping) {
				// cancel
				mDownView.animate().translationX(0).alpha(1)
						.setDuration(mAnimationTime).setListener(null);
			}
			mVelocityTracker.recycle();
			mVelocityTracker = null;
			mDownX = 0;
			mDownY = 0;
			mDownView = null;
			mDownPosition = ListView.INVALID_POSITION;
			mSwiping = false;
			break;
		}

		case MotionEvent.ACTION_UP: {
			if (mVelocityTracker == null) {
				break;
			}

			float deltaX = motionEvent.getRawX() - mDownX;
			mVelocityTracker.addMovement(motionEvent);
			mVelocityTracker.computeCurrentVelocity(1000);
			float velocityX = mVelocityTracker.getXVelocity();
			float absVelocityX = Math.abs(velocityX);
			float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
			boolean dismiss = false;
			boolean dismissRight = false;
			if (Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
				dismiss = true;
				dismissRight = deltaX > 0;
			} else if (mMinFlingVelocity <= absVelocityX
					&& absVelocityX <= mMaxFlingVelocity
					&& absVelocityY < absVelocityX && mSwiping) {
				// dismiss only if flinging in the same direction as dragging
				dismiss = (velocityX < 0) == (deltaX < 0);
				dismissRight = mVelocityTracker.getXVelocity() > 0;
			}
			if (dismiss && mDownPosition != ListView.INVALID_POSITION) {
				// dismiss
				final View downView = mDownView; // mDownView gets null'd before
													// animation ends
				final int downPosition = mDownPosition;
				synchronized (mAnimationLock) {
					if (mAnimatedViews.contains(downView)) {
						break;
					}
					++mDismissAnimationRefCount;
					mAnimatedViews.add(downView);
				}
				mDownView.animate()
						.translationX(dismissRight ? mViewWidth : -mViewWidth)
						.alpha(0).setDuration(mAnimationTime)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								performDismiss(downView, downPosition);
							}
						});
			} else {
				// cancel
				mDownView.animate().translationX(0).alpha(1)
						.setDuration(mAnimationTime).setListener(null);
			}
			mVelocityTracker.recycle();
			mVelocityTracker = null;
			mDownX = 0;
			mDownY = 0;
			mDownView = null;
			mDownPosition = ListView.INVALID_POSITION;
			mSwiping = false;
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			if (mVelocityTracker == null || mPaused) {
				break;
			}

			mVelocityTracker.addMovement(motionEvent);
			float deltaX = motionEvent.getRawX() - mDownX;
			float deltaY = motionEvent.getRawY() - mDownY;
			if (Math.abs(deltaX) > mSlop
					&& Math.abs(deltaY) < Math.abs(deltaX) / 2) {
				mSwiping = true;
				mSwipingSlop = (deltaX > 0 ? mSlop : -mSlop);
				mRecyclerView.requestDisallowInterceptTouchEvent(true);

				// Cancel ListView's touch (un-highlighting the item)
				MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
				cancelEvent
						.setAction(MotionEvent.ACTION_CANCEL
								| (motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
				mRecyclerView.onTouchEvent(cancelEvent);
				cancelEvent.recycle();
			}

			if (mSwiping) {
				mDownView.setTranslationX(deltaX - mSwipingSlop);
				mDownView.setAlpha(Math.max(0f,
						Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth)));
				return true;
			}
			break;
		}
		}
		return false;
	}

	/**
	 * The Class PendingDismissData.
	 */
	class PendingDismissData implements Comparable<PendingDismissData> {
		
		/** The position. */
		public int position;
		
		/** The view. */
		public View view;

		/**
		 * Instantiates a new pending dismiss data.
		 *
		 * @param position the position
		 * @param view the view
		 */
		public PendingDismissData(int position, View view) {
			this.position = position;
			this.view = view;
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(PendingDismissData other) {
			// Sort by descending position
			return other.position - position;
		}
	}

	/**
	 * Perform dismiss.
	 *
	 * @param dismissView the dismiss view
	 * @param dismissPosition the dismiss position
	 */
	private void performDismiss(final View dismissView,
			final int dismissPosition) {
		// Animate the dismissed list item to zero-height and fire the dismiss
		// callback when
		// all dismissed list item animations have completed. This triggers
		// layout on each animation
		// frame; in the future we may want to do something smarter and more
		// performant.

		final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
		final int originalHeight = dismissView.getHeight();

		ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1)
				.setDuration(mAnimationTime);

		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				boolean noAnimationLeft;
				synchronized (mAnimationLock) {
					--mDismissAnimationRefCount;
					mAnimatedViews.remove(dismissView);
					noAnimationLeft = mDismissAnimationRefCount == 0;
				}
				if (noAnimationLeft) {
					// No active animations, process all pending dismisses.
					// Sort by descending position
					Collections.sort(mPendingDismisses);

					int[] dismissPositions = new int[mPendingDismisses.size()];
					for (int i = mPendingDismisses.size() - 1; i >= 0; i--) {
						dismissPositions[i] = mPendingDismisses.get(i).position;
					}
					mCallbacks.onDismiss(mRecyclerView, dismissPositions);

					// Reset mDownPosition to avoid MotionEvent.ACTION_UP trying
					// to start a dismiss
					// animation with a stale position
					mDownPosition = ListView.INVALID_POSITION;

					ViewGroup.LayoutParams lp;
					for (PendingDismissData pendingDismiss : mPendingDismisses) {
						// Reset view presentation
						pendingDismiss.view.setAlpha(1f);
						pendingDismiss.view.setTranslationX(0);
						lp = pendingDismiss.view.getLayoutParams();
						lp.height = originalHeight;
						pendingDismiss.view.setLayoutParams(lp);
					}

					// Send a cancel event
					long time = SystemClock.uptimeMillis();
					MotionEvent cancelEvent = MotionEvent.obtain(time, time,
							MotionEvent.ACTION_CANCEL, 0, 0, 0);
					mRecyclerView.dispatchTouchEvent(cancelEvent);

					mPendingDismisses.clear();
				}
			}
		});

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				dismissView.setLayoutParams(lp);
			}
		});

		mPendingDismisses.add(new PendingDismissData(dismissPosition,
				dismissView));
		animator.start();
	}
}
