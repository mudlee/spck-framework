package spck.core;

public class DesktopWindowPreferences {
	public Antialiasing antialiasing = Antialiasing.OFF;
	public boolean fullscreen;
	public int width = 1024;
	public int height = 768;
	public String title = "SPCK";
	public boolean vsync;

	@Override
	public String toString() {
		return "DesktopWindowPreferences{" +
				"antialiasing=" + antialiasing +
				", fullscreen=" + fullscreen +
				", width=" + width +
				", height=" + height +
				", title='" + title + '\'' +
				", vsync=" + vsync +
				'}';
	}

	public static final class Builder {
		private Antialiasing antialiasing = Antialiasing.OFF;
		private boolean fullscreen;
		private int width = 1024;
		private int height = 768;
		private String title = "SPCK";
		private boolean vsync;

		private Builder() {
		}

		public static Builder create() {
			return new Builder();
		}

		public Builder withAntialiasing(Antialiasing antialiasing) {
			this.antialiasing = antialiasing;
			return this;
		}

		public Builder withFullscreen(boolean fullscreen) {
			this.fullscreen = fullscreen;
			return this;
		}

		public Builder withWidth(int width) {
			this.width = width;
			return this;
		}

		public Builder withHeight(int height) {
			this.height = height;
			return this;
		}

		public Builder withTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder withVsync(boolean vsync) {
			this.vsync = vsync;
			return this;
		}

		public DesktopWindowPreferences build() {
			DesktopWindowPreferences desktopWindowPreferences = new DesktopWindowPreferences();
			desktopWindowPreferences.vsync = this.vsync;
			desktopWindowPreferences.title = this.title;
			desktopWindowPreferences.width = this.width;
			desktopWindowPreferences.fullscreen = this.fullscreen;
			desktopWindowPreferences.antialiasing = this.antialiasing;
			desktopWindowPreferences.height = this.height;
			return desktopWindowPreferences;
		}
	}
}
