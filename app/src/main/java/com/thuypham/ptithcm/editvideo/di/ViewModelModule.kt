package com.thuypham.ptithcm.editvideo.di

import com.thuypham.ptithcm.editvideo.viewmodel.CutViewModel
import com.thuypham.ptithcm.editvideo.viewmodel.ExtractImageViewModel
import com.thuypham.ptithcm.editvideo.viewmodel.HomeViewModel
import com.thuypham.ptithcm.editvideo.viewmodel.MediaViewModel
import com.thuypham.ptithcm.editvideo.viewmodel.MergeViewModel
import com.thuypham.ptithcm.editvideo.viewmodel.ResultViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule = module {
    viewModel { HomeViewModel() }
    viewModel { MediaViewModel(get()) }
    viewModel { ResultViewModel(get()) }
    viewModel { ExtractImageViewModel() }
    viewModel { MergeViewModel(get()) }
    viewModel { CutViewModel() }
}
