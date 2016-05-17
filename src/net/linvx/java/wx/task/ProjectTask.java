/**
 * 
 */
package net.linvx.java.wx.task;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import net.linvx.java.libs.tools.MyLog;

/**
 * 
 * @author lizelin
 *
 */
public class ProjectTask implements ServletContextListener {
	private static final Logger log = MyLog.getLogger(ProjectTask.class);
	// 定时器
	private Timer timer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
	 * ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		timer.cancel();// 定时器销毁
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.ServletContextListener#contextInitialized(javax.servlet.
	 * ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		log.info("ProjectTask.contextInitialized execute!");
		timer = new Timer(true);
		// timer.schedule(new FansInfoUpdateTask(), 300 * 1000);
		timer.schedule(new NormalTask(), 60 * 1000);

	}

	class NormalTask extends TimerTask {

		@Override
		public void run() {

		}

	}
}
