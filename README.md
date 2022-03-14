# ECMFA 2022


## Guides

## How to get it work?
In order to use presented approach, users need to follow the following steps in the same order:
1. Download Eclipce, can be downloaded from [here](https://www.eclipse.org/downloads/).
2. Download Epsilon, can be downloaded from [here](https://www.eclipse.org/epsilon/download/).
3. Import Epsilon source code, can be downloaded from [here](https://www.eclipse.org/epsilon/download/#source-code).
4. Delet the Egl engine package from the source code, can be found under this name org.eclipse.epsilon.egl.engine.
5. Import the new Egl engine version (called org.eclipse.epsilon.egl.engine), can be found [here](https://drive.google.com/file/d/11cjBCZk2A98cPZXfEjo__vwyF27XPKv3/view?usp=sharing).
6. Import the Sync engine package (called org.eclipse.epsilon.egl.sync), can be found [here](https://drive.google.com/file/d/1oWZl3BhnY1tCRnY-X2bX1HElzPvP5_YW/view?usp=sharing).




### Generalisability Experiment
- Runner class (as JUnit test) can be found [here](https://github.com/soha500/EglSyncNew/blob/master/org.eclipse.epsilon.egl.sync/src/org/eclipse/epsilon/egl/sync/SyncGenerelisabiltyTests.java).
- The models and generated text files can be downloaded from [here](https://drive.google.com/file/d/1Hgi92cQ9tnab9J_0hz5j9IU2zkWjUZKw/view?usp=sharing).

### Correctness Experiment
- Runner class (as JUnit test) can be found [here](https://github.com/soha500/EglSyncNew/blob/master/org.eclipse.epsilon.egl.sync/src/org/eclipse/epsilon/egl/sync/SyncCorrectnessTests.java).
- The models and generated text files can be downloaded from [here](https://drive.google.com/file/d/1rrXCMSwvGpcH_buC9cMcLvmWREOoPdD7/view?usp=sharing).

### Performance/Scalability Experiment 
- Runner class (as JUnit tests) can be found [here](https://github.com/soha500/EglSyncNew/blob/master/org.eclipse.epsilon.egl.sync/src/org/eclipse/epsilon/egl/sync/SyncScalablityTests.java).
- The models and generated text files can be downloaded from [here](https://drive.google.com/file/d/1zWDiOvRI0FLbCsUXWfAQihWVovIU9P7J/view?usp=sharing).
- The raw results and the analysis in Excel can be found [here](https://github.com/soha500/EglSyncNew/blob/master/DataForScalabiltyPerformanceTests.xlsx).
