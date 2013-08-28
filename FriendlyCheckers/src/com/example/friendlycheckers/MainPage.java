package com.example.friendlycheckers;
    
    public class MainPage
    {
    	public enum GameState { OUT_OF_GAME, END_GAME, OPTIONS, ABOUT, MENU, SINGLE_PLAYER, LOCAL_MULTI, WAITING_FOR_COMPUTER };
        public static int w = 400, h = 400;
        public static Boolean FORCE_JUMP = false, DIFFICULT = false;
        public static GameState game_state = GameState.MENU;
        private int hi; //placeholder until I can find something useful to do with this constructor

        public MainPage()
        {
        	this.hi=10; //lol?
        }
        private void SinglePlayer_Setup()
        {
            game_state = GameState.SINGLE_PLAYER;
            //switch to gamePage
        }
        private void Local_Multi_Setup()
        {
            game_state = GameState.LOCAL_MULTI;
           //switch to gamePage
        }
        private Boolean InLocalGame()
        {
            return (game_state == GameState.SINGLE_PLAYER || game_state==GameState.LOCAL_MULTI);
        }
        private Boolean MenuState()
        {
            return (game_state == GameState.MENU || game_state == GameState.ABOUT || game_state == GameState.OPTIONS);
        }
        private void Show_Options()
        {
            game_state = GameState.OPTIONS;
            //switch to settingsPage
        }
        private void Show_About()
        {
            game_state = GameState.ABOUT;
            // switch to about page
        }
        public GameState getGameType()
        {
            return game_state;
        }

}